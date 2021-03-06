package exact;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import bn.core.*;
import bn.parser.*;

/**
 * 
 * @author Steven Allaben
 *
 */
public class ExactInferer {

	BayesianNetwork bn;
	Assignment evidence;
	RandomVariable query;
	
	/*
	 * Constructor. Reads in a .xml or .bif file from the filesystem in the /networks/ folder.
	 * Uses Prof. Ferguson's parser code to interpret the network file as a BayesianNetwork.
	 */
	public ExactInferer(String filename, String query, Assignment evidence) 
			throws IOException, ParserConfigurationException, SAXException {
		if(filename.endsWith(".xml")) {
			XMLBIFParser xbp = new XMLBIFParser();
			bn = xbp.readNetworkFromFile(filename);
		} else if(filename.endsWith(".bif")) {
			InputStream input = new FileInputStream(filename);
			BIFLexer bl = new BIFLexer(input);
			BIFParser bp = new BIFParser(bl);
			bn = bp.parseNetwork();
		} else {
			System.err.println("Input file [" + filename + "] does not have extension .xml or .bif!");
			System.exit(5);
		}
		evidence.match(bn.getVariableList());
		this.evidence = evidence;
		this.query = bn.getVariableByName(query);
	}
	
	/*
	 * ENUMERATION-ASK algorithm from AIMA
	 */
	public Distribution ask() {
		Distribution d = new Distribution(query);
		Assignment possible;
		for(Object value : query.getDomain()) {
			possible = evidence.extend(query, value);
			d.put(value, probability(bn.getVariableListTopologicallySorted(), possible));
		}
		d.normalize();
		return d;
	}
	
	/*
	 * ENUMERATION-ALL algorithm from AIMA
	 */
	public double probability(List<RandomVariable> variables, Assignment e) {
		if(variables.isEmpty()) {
			return 1.0;
		}
		List<RandomVariable> vars = new ArrayList<>();
		vars.addAll(variables);
		RandomVariable first = vars.remove(0);
		if(e.containsKey(first)) {
			return bn.getProb(first, e) * probability(vars, e);
		} else {
			double sum = 0.0;
			Assignment possible;
			for(Object value : first.getDomain()) {
				possible = e.extend(first, value);
				sum += bn.getProb(first, possible) * probability(vars, possible);
			}
			return sum;
		}
	}
	
}
