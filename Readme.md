
Carti-Rank: Finding Subspace Clusters using Ranked Neighborhoods
================================================================

This project is an implementation of the Carti-Rank algorithm. The details can
be found in http://adrem.uantwerpen.be/cartirank .


Application
-----------
Carti-Rank is packaged as a runnable .jar file (carti-rank.jar). You can run the application
 on command line with the commands,

```
java -jar carti-rank.jar data-file theta minlen
```


Input
-----
Carti-Rank accepts parameters as command line arguments in a specified order.

- `data-file`: Path to the multi dimensional datafile. Please find the
properties of the data file below.
- `k`: Parameter for the tile mining.
- `minsup`: Minimum length for the cluster.

### About the data file:

- it includes space separated real values,
- each row represents an instance,
- each column represents a feature/dimension,
- does not include a(ny) header row(s),
- all instances have the same number of features,
- there are no missing values,
- the real values formatted in the USA locale (use . as decimal separator)


Output
------
Carti-Rank outputs the found clusters to the standard output. Each line of
output represents a subspace cluster. Output format:
```
Size of the cluster - Dimensions of the cluster - Objects of the cluster
```
For example, ```10 - 1 2 6 - 0 1 2 3 4 5 6 7 8 9``` means a cluster is
detected at 1st, 2nd and 6th subspaces and it has '10' objects, i.e.,
0 1 2 3 4 5 6 7 8 9


Example
-------

```
java -jar carti-rank.jar ns25.mime 25 20
```

Datasets
--------
Datasets that are used in the original paper can be found [here][datasets].

Building the Application
------------------------

Get the latest source code from https://gitlab.com/adrem/carti-rank by using the
command

```
git clone https://gitlab.com/adrem/carti-rank.git
```

This implementation uses [Apache Maven](https://maven.apache.org/) for
dependency management and building. To create an executable `.jar` file, execute
the following command

```
mvn clean compile assembly:single
```

An executable jar file will be created in `target` directory. See above for
running instructions.

Contact
-------
For more information you can visit http://adrem.uantwerpen.be/cartirank or send
an email to [Emin Aksehirli][] <emin.aksehirli@gmail.com>.

[data-sets]:http://adrem.uantwerpen.be/sites/adrem.ua.ac.be/files/carti-rank_datasets.tar.bz2
[Emin Aksehirli]:http://memin.tk
