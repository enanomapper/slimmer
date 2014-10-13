package com.github.enanomapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLEntityRemover;

public class Slimmer {

	public static void main(String[] args) {
		String rootFolder = args[0];
		System.out.println("Searching configuration files in " + rootFolder);
		File dir = new File(rootFolder);
		File[] files = dir.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.toLowerCase().endsWith(".props");
		    }
		});
		for (File file : files) {
			try {
				System.out.println("Slimming for " + file.getName());
				Properties props = new Properties();
				props.load(new FileReader(file));
				String owlFilename = props.getProperty("owl");
				File owlFile = new File(owlFilename);
				OWLOntologyManager man = OWLManager.createOWLOntologyManager();
				OWLOntology onto = man.loadOntology(
					IRI.create("file://" + owlFile.getAbsoluteFile())
				);
				System.out.println("Loaded axioms: " + onto.getAxiomCount());

				String iriFilename = props.getProperty("iris");
				BufferedReader reader = new BufferedReader(new FileReader(
					new File(rootFolder,iriFilename)
				));
				String line = reader.readLine();
				Set<String> irisToSave = new HashSet<String>();
				while (line != null) {
					String iri = line.trim();
					System.out.println("Extracting " + iri + "...");
					irisToSave.add(iri);
					line = reader.readLine();
				}
				reader.close();

				OWLEntityRemover remover = new OWLEntityRemover(
					man, Collections.singleton(onto)
				);
				for (OWLClass ind : onto.getClassesInSignature()) {
					String indIRI = ind.getIRI().toString();
					System.out.println(indIRI);
					if (!irisToSave.contains(indIRI)) {
						System.out.println("Remove: " + indIRI);
						ind.accept(remover);
					}
				}
				man.applyChanges(remover.getChanges());

				// save in OWL/XML format
				File output = File.createTempFile("saved_pizza", "owl");
				IRI documentIRI2 = IRI.create(output);
				man.saveOntology(onto, new OWLXMLOntologyFormat(), documentIRI2);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}