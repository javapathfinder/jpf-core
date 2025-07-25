## Randomization Options in JPF ##

The randomization options in JPF allow the user to experiment in randomizing the order of choices explored. 

`cg.randomize_choices` can have three possible values: random, path, def.

 * `random`: It explores random choices during program execution with **varying results among different trials**. The default seed used to generate different results is the **system time in milliseconds**

 * `path`: It explores random choices during program execution with **reproducible results among different trials**. The default seed used to generate reproducible results is **42**. The value of the seed can be changed by setting the seed config option.

 * `def`: No randomization, choices are explored using the default search order imposed by the model checker. 

`cg.seed (_INT_)`: The user can specify a particular seed for the random number generator in order to obtain reproducible results in the presence of randomization. Note that this is effective only when the `path` option of `randomize_choices` is selected. 


