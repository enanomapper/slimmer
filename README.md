
Slimmer is a slim tool to slim ontologies as part of ontology integration. It allows users to
provide configuration files that specify which parts of an ontology should be kept and/or
removed, allowing to just select parts of the ontology you like. Rewiring the ontology is part
of the features, allowing you to define new super terms.

Configuring the slimming
------------------------

The configuration of the slimming of an ontology consists of two files. The first file is a
Java properties file, listing the ontology, a pointer to the second configuration file, and
the URI of the output ontology. For example:

    owl=http://www.bioassayontology.org/bao/bao_complete.owl
    iris=bao.iris
    slimmed=http://purl.enanomapper.org/onto/external/bao-slim.owl

The second, .iris file configures the slicing of the ontology:

    +D(http://purl.bioontology.org/ontology/npo#NPO_1436):http://www.bioassayontology.org/bao#BAO_0000697 detection instrument
    +D(http://purl.obolibrary.org/obo/IAO_0000030):http://www.bioassayontology.org/bao#BAO_0000179 endpoint
    +D(http://purl.obolibrary.org/obo/OBI_0000070):http://www.bioassayontology.org/bao#BAO_0000015 bioassay
 
This configuration file uses a custom syntax which is briefly explained here. By default it removes all content.

The first character indicates if the something needs to be included (+) or excluded from a previously defined
inclusion (-). The second character indicates whether a whole upper (U) or down (D) tree should be included or
excluded. After the colon the URI of the resource is given to be in- or excluded, followed by a user-oriented
comment. Finally, before the colon and in brackets an optional superclass of this resource can be specified,
possibly from other ontologies.

Compile
-------

    $ mvn assembly:assembly

Run
---

    $ java -cp target/slimmer-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.github.enanomapper.Slimmer src/main/resources
