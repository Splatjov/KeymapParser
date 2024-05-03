# Arithmetic Parser

This is a test task for a JetBrains:)

### About realization:

- Recursive Down Parser (used it for simplicity and maximal abstraction)
- Almost every element in model is a class
- Abstract enough (No magic values, easy to add own Expressions or Operations)
- Fast! Tried to use memory and time as effective as possible (f.e. used only one string inside all recursion)
- Almost no reoccurring code: thanks to **class reflections** operations/expressions are combined, even in testing!
- Informative exceptions! 
- Trendy **coroutines**: since it's recursive, we can use coroutines to divide and perform calculations (so in some cases it will be much faster than incremental parser)
- Not just using **coroutines** everywhere: actually you can disable it if you have f.e. small number of cpu cores or special input, and it will be faster
- Easy to keep track, use everywhere you like and debug: not only we receive a real tree with right parent-child class relations as a result while using O(1) memory, we also have a segment indexes in string
- Customisable: you can set an 'element' value, use long integer instead of a regular one in no time
- Simple nevertheless informative and well-covered tests, with use of every case

### What I would do if I had more time:
- Support for spaces (it's an easy one to add, but you need to make both versions in case space is prohibited)
- Coroutine queue (since we're just doing CPU-bound work, we actually need just *CPU-cores*-coroutines)