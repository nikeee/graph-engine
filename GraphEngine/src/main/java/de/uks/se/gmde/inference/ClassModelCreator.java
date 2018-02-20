package de.uks.se.gmde.inference;

import de.uks.se.gmde.Graph;
import de.uks.se.gmde.Node;
import de.uks.se.gmde.inference.naming.GenericToSpecific;
import de.uks.se.gmde.inference.naming.SpecificToGeneric;
import de.uniks.networkparser.graph.Attribute;
import de.uniks.networkparser.graph.Cardinality;
import de.uniks.networkparser.graph.Clazz;
import de.uniks.networkparser.graph.DataType;
import de.uniks.networkparser.interfaces.SendableEntity;
import de.uniks.networkparser.interfaces.SendableEntityCreator;
import org.sdmlib.models.classes.ClassModel;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClassModelCreator
{
	public static ClassModel inferClassModel(Graph state, String packageName)
	{
		// loop through nodes and getOrCreate classes
		ClassModel cm = new ClassModel(packageName);
		for (Node node : state.getNodes())
		{
			String typeName = GenericToSpecific.inferTypeName(node);
			Clazz c = cm.getClazz(typeName);
			if (c == null)
				c = cm.createClazz(typeName); // Side-effect puts it into cm

			inferAttributes(node, c);
		}

		Map<String, Map<String, Map<String, Cardinality>>> cardinalities = inferCardinalities(state.getNodes());

		// loop through edges and gerOrCreate associations
		cardinalities.forEach((typeName, cards) -> {
			Clazz source = cm.getClazz(typeName);
			assert source != null;

			cards.forEach((assocName, destinations) -> {
				destinations.forEach((destTypeName, cardinality) -> {

					Clazz dest = cm.getClazz(destTypeName);
					assert dest != null;

					source.withUniDirectional(dest, assocName, cardinality);
				});
			});
		});
		return cm;
	}

	private static Map<String, Map<String, Map<String, Cardinality>>> inferCardinalities(List<Node> nodes)
	{
		assert nodes != null;

		Map<String, Map<String, Map<String, Cardinality>>> cardinalityMap = new HashMap<>(nodes.size());
		for (Node node : nodes)
		{
			String type = GenericToSpecific.inferTypeName(node);
			Map<String, Map<String, Cardinality>> edgeLabelTypeCardMap = cardinalityMap.computeIfAbsent(type, __ -> new HashMap<>());
			node.getEdges().forEach((label, destinations) -> {
				assert label != null;
				if (destinations == null || destinations.size() <= 0)
					return;

				String assocName = GenericToSpecific.inferAssocName(label);
				Map<String, Cardinality> destCardMap = edgeLabelTypeCardMap.computeIfAbsent(assocName, __ -> new HashMap<>());

				Map<String, Long> destCards = destinations.stream()
					.map(GenericToSpecific::inferTypeName)
					.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

				destCards.forEach((destTypeName, destCount) -> {
					Cardinality cardinality = destCardMap
						.computeIfAbsent(destTypeName, __ -> destCount == 1 ? Cardinality.ONE : Cardinality.MANY);
					if (Cardinality.ONE.equals(cardinality) && destCount > 1)
						destCardMap.put(destTypeName, Cardinality.MANY);
				});
			});
		}
		return cardinalityMap;
	}

	private static void inferAttributes(Node node, Clazz c)
	{
		// loop through attributes and gerOrCreate in classes
		node.getAttributes().forEach((key, value) -> {
			// NOT using Java 8 Streams
			// Instead, using Non-Lazy-Evaluated-SDMLib-NetworkParser-Stuff
			// (To use Java-8-APIs, use .stream().filter(predicate))
			Attribute attr = c.getAttributes().filter(a -> key.equals(a.getName())).first();

			if (attr == null)
			{
				DataType type = TypeSystem.inferDataType(value);
				String attrName = GenericToSpecific.inferAttributeName(key);
				c.createAttribute(attrName, type);
			}
			else
			{
				// Attribute already exists, possible with a different type
				DataType possibleWiderType = TypeSystem.inferDataType(value);
				if (!attr.getType().equals(possibleWiderType))
				{
					// The type is different, we have to adjust it to fit both values
					DataType widerType = TypeSystem.widenType(attr.getType(), possibleWiderType);
					String msg = String.format(
						"Got attribute \"%s\" with different types: (current: %s, new: %s), widening to %s",
						attr.getName(),
						attr.getType(),
						possibleWiderType,
						widerType
					);
					System.out.println(msg);
					attr.with(widerType);
				}
			}
		});
	}

	public static Map<Node, Object> genericToSpecific(Graph state, String packageName)
	{
		final Map<Node, Object> objectMap = new HashMap<>(state.getNodes().size());
		final Map<Node, SendableEntityCreator> nodeCreatorMap = createCreatorMap(state, packageName);

		for (Node node : state.getNodes())
		{
			SendableEntityCreator currentCreator = nodeCreatorMap.get(node);
			assert currentCreator != null;

			SendableEntity objectInstance = (SendableEntity) currentCreator.getSendableInstance(false);
			objectMap.put(node, objectInstance);

			node.getAttributes().forEach((attributeName, attributeValue) -> {

				// Skip label field, because it's the type
				if (Node.LABEL_ATTRIBUTE_NAME.equals(attributeName))
					return;

				String fieldName = GenericToSpecific.inferPrivateFieldName(attributeName);

				currentCreator.setValue(objectInstance, fieldName, attributeValue, null);
			});
		}


		for (Node node : state.getNodes())
		{
			final Object sourceObject = objectMap.get(node);
			if (sourceObject == null)
				throw new IllegalStateException("sourceObject is null");

			SendableEntityCreator sourceCreator = nodeCreatorMap.get(node);

			node.getEdges().forEach((label, targets) -> {
				String assocName = GenericToSpecific.inferAssocName(label);
				assert assocName != null;

				if (Arrays.stream(sourceCreator.getProperties()).noneMatch(assocName::equals))
					throw new IllegalStateException("Did not find assoc name in class model");

				for (Node destination : targets)
				{
					final Object destObject = objectMap.get(destination);
					if (destObject == null)
						throw new IllegalStateException("destObject is null");

					sourceCreator.setValue(sourceObject, assocName, destObject, null);
				}
			});
		}
		return objectMap;
	}

	private static Map<Node, SendableEntityCreator> createCreatorMap(Graph state, String packageName)
	{
		return state.getNodes().stream()
			.collect(Collectors.toMap(
				n -> n,
				n -> loadCreator(n, packageName)
			));
	}

	public static Graph specificToGeneric(SendableEntity... entities)
	{
		Graph result = new Graph();

		Queue<SendableEntity> todo = new LinkedList<>(Arrays.asList(entities));

		Map<SendableEntity, Node> entityNodeMap = new HashMap<>(todo.size());
		Map<SendableEntity, SendableEntityCreator> creatorMap = new HashMap<>(todo.size());

		while (!todo.isEmpty())
		{
			SendableEntity currentItem = todo.poll();
			assert !entityNodeMap.containsKey(currentItem);

			Class<?> itemClass = currentItem.getClass();
			String typeName = itemClass.getSimpleName();

			Node currentItemNode = result.createNode().withLabel(SpecificToGeneric.inferNodeLabel(typeName));
			entityNodeMap.put(currentItem, currentItemNode);

			SendableEntityCreator creator = loadCreator(itemClass);
			creatorMap.put(currentItem, creator);

			for (String property : creator.getProperties())
			{
				Object attributeValue = creator.getValue(currentItem, property);
				if (attributeValue == null)
					continue;


				if (attributeValue instanceof Collection)
				{
					// it's a set, pointing to other objects
					for (Object o : ((Collection) attributeValue))
					{
						if (!(o instanceof SendableEntity))
							throw new IllegalStateException("Collection contained an object that does not implement SendableEntity");

						SendableEntity se = (SendableEntity) o;
						if (!todo.contains(se) && entityNodeMap.get(se) == null)
							todo.add(se);
					}
				}
				else if (attributeValue instanceof SendableEntity)
				{
					// it's a reference to some other object
					if (!todo.contains(attributeValue) && entityNodeMap.get(attributeValue) == null)
						todo.add((SendableEntity) attributeValue);
				}
			}
		}

		// loop through every stuff again
		for (Map.Entry<SendableEntity, Node> entry : entityNodeMap.entrySet())
		{
			SendableEntityCreator creator = creatorMap.get(entry.getKey());
			if (creator == null)
				throw new IllegalStateException("creator is null");

			SendableEntity currentItem = entry.getKey();
			Node currentItemNode = entityNodeMap.get(currentItem);

			for (String property : creator.getProperties())
			{
				Object attributeValue = creator.getValue(currentItem, property);
				if (attributeValue == null)
					continue;

				if (attributeValue instanceof Collection)
				{
					String assocName = SpecificToGeneric.edgeLabel(property);
					// it's a set, pointing to other objects
					for (Object o : ((Collection) attributeValue))
					{
						if (!(o instanceof SendableEntity))
							throw new IllegalStateException("Collection contained an object that does not implement SendableEntity");

						SendableEntity se = (SendableEntity) o;
						Node dest = entityNodeMap.get(se);
						if (dest == null)
							throw new IllegalStateException("dest is null");

						currentItemNode.createEdge(assocName, dest);
					}
				}
				else if (attributeValue instanceof SendableEntity)
				{
					Node dest = entityNodeMap.get(attributeValue);
					if (dest == null)
						throw new IllegalStateException("dest is null");

					String assocName = SpecificToGeneric.edgeLabel(property);
					currentItemNode.createEdge(assocName, dest);
					// it's a reference to some other object
				}
				else
				{
					String attrName = SpecificToGeneric.inferAttributeName(property);
					// it's something else, just set it
					currentItemNode.withAttr(attrName, attributeValue);
				}
			}
		}

		return result;
	}

	private static SendableEntityCreator loadCreator(Node forNode, String packageName)
	{
		String creatorFullTypeName = GenericToSpecific.getCreatorTypeName(forNode, packageName);
		return loadCreator(creatorFullTypeName);
	}

	private static SendableEntityCreator loadCreator(Class<?> forType)
	{
		String creatorFullTypeName = SpecificToGeneric.getCreatorFullTypeName(forType);
		return loadCreator(creatorFullTypeName);
	}

	private static SendableEntityCreator loadCreator(String creatorFullTypeName)
	{
		final ClassLoader cl = ClassLoader.getSystemClassLoader();
		try
		{
			final Class<?> c = cl.loadClass(creatorFullTypeName);
			return (SendableEntityCreator) c.getConstructor().newInstance();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
