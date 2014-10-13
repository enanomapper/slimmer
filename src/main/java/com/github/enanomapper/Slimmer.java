package com.github.enanomapper;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.Properties;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

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
			} catch (Exception e) {
				System.err.println("Cannot read file: " + file.getAbsoluteFile());
				e.printStackTrace();
			}
		}
	}

}