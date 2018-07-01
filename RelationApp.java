import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

// Main driver class of the program
public class RelationApp {
	private static Scanner sc;
	private static Boolean useUnicode;
	
	// Main method run from console
	public static void main(String[] args) {
		boolean exit = false;
		sc = new Scanner(System.in);
		useUnicode = true;
		
		while(!exit) {
			System.out.println("Select an option:");
			System.out.println(" 1) Enter a relation matrix manually");
			System.out.println(" 2) Load relation matrix from file");
			System.out.println(" 3) Switch to " + (useUnicode ? "ASCII" : "Unicode") + " box drawing");
			System.out.println(" 4) Exit");
			System.out.print("  >");
			
			Relation r = null;
			int option = -1;
			while(option < 0) {
				try {
					option = Integer.parseInt(sc.nextLine());
				} catch(NumberFormatException e) {
					System.out.print("  >");
				}
			}
			switch(option) {
			case 1:
				r = promptRelation();
				break;
			case 2:
				System.out.print("Enter filename:\n>");
				r = relationFromFile(sc.nextLine());
				break;
			case 3:
				useUnicode = !useUnicode;
				break;
			case 4:
				exit = true;
				break;
			default:
			}
			if(r != null) {
				printRelationProperties(r);
				System.out.println("\nPress enter to continue...");
				sc.nextLine();
			}
		}
		
		sc.close();
	}
	
	// Prompts the user to enter a relation's matrix into the console manually
	public static Relation promptRelation() {
		System.out.print("Enter number of variables:\n  >");
		Relation r = new Relation(sc.nextInt());
		sc.nextLine();
		System.out.println("Enter values for each row, separated by spaces:");
				
		for(int i = 0; i < r.nElements(); i++) {
			int j = 0;
			String entries = "";
			while(j < r.nElements()) {
				System.out.print((i + 1) + " >" + entries);
				String line = sc.nextLine();
				Scanner rs = new Scanner(line);
				while(rs.hasNext() && j < r.nElements()) {
					String s = rs.next();
					if(s.equals("1") || s.equals("0")) {
						r.set(i, j, s.equals("1"));
						j++;
						entries += s + " ";
					}
				}
				rs.close();
			}
			
		}
		
		return r;
	}
	
	// Loads a relation from the given text file
	// See 'relation-file-guide.txt' for how to format the text files
	public static Relation relationFromFile(String filename) {
		File file = new File(filename);
		Relation r = null;
		try {
			Scanner sc = new Scanner(file);
			int nElements = Integer.parseInt(sc.nextLine());
			r = new Relation(nElements);
			int i = 0;
			while(i < nElements) {
				int j = 0;
				while(j < nElements) {
					if(!sc.hasNext()) {
						System.out.println("Error: unexpected end of file. Could not parse " + filename + "\n");
						sc.close();
						return null;
					}
					String s = sc.next();
					if(s.equals("1")) {
						r.set(i, j, true);
						j++;
					} else if(s.equals("0")) {
						j++;
					}
				}
				i++;
			}
			sc.close();
		} catch(FileNotFoundException e) {
			System.out.println("Could not find file " + filename + "\n");
			return null;
		} catch(NumberFormatException e) {
			System.out.println("Error while parsing " + filename + ", check formatting and try again\n");
		}
		return r;
	}
	
	// Print an analysis of the given relation to the console
	public static void printRelationProperties(Relation r) {
		System.out.println("Loaded relation matrix:\n");
		System.out.println(r.toString(useUnicode) + "\n");
		
		System.out.println("This relation is " + (r.isReflexive() ? "" : "NOT ") + "reflexive.");
		System.out.println("This relation is " + (r.isSymmetric() ? "" : "NOT ") + "symmetric.");
		System.out.println("This relation is " + (r.isAntisymmetric() ? "" : "NOT ") + "antisymmetric.");
		System.out.println("This relation is " + (r.isTransitive() ? "" : "NOT ") + "transitive.");
		System.out.println("This relation is " + (r.isEquivalence() ? "" : "NOT ") + "an equivalence relation.");
		
		if(!r.isTransitive()) {
			System.out.println("\nThe transitive closure of this relation is:\n");
			System.out.println(r.transitiveClosure().toString(useUnicode));
		}
		
		if(r.isEquivalence()) {
			System.out.println("\nThe equivalence classes of this relation are:");
			System.out.println(r.equivClassesAsString());
		}
	}
}