# Graph Engine
> Unmaintained university project form 2017/18 of the class Graph & Model-Driven Engineering


Its features:
- capable of state transformations of a [labelled transition system](https://en.wikipedia.org/wiki/Transition_system).
- You can apply rules based on matched patterns as well test for isomorphism.
- rules that can check whether certain conditions are met (All/Exists Globally/Next/Until), which represent a subset of [Computational Tree Logic (CTL)](https://en.wikipedia.org/wiki/Computational_tree_logic).
    - Using these, you can find the solution to the [Wolf, goat and cabbage problem](https://en.wikipedia.org/wiki/Wolf,_goat_and_cabbage_problem) by building an LTS/state-reachability-graph and checking for paths in the LTS where no violation occurs.
    - See `GraphEngine/src/test/java/de/uks/se/gmde/problem/ferryman` for an example
- (De)Serialization
- GraphViz Rendering
- Model transformations for Java class hierarchies
- Probably some other stuff I forgot

You can find the (German) presentation at: https://nikeee.github.io/graph-engine

## Compile
```bash
cd GraphEngine
./gradlew compileJava
```

Run tests and generate some SVGs:
```bash
./gradlew test
```

You can also find them in `GraphEngine/dumps`

