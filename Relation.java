import java.util.ArrayList;

// Java object representing a relation of a set of n elements. The relation is modeled via a matrix, where
// if the element in the ith row and jth column is 1, then i relates j, and if not, i does not relate j.
public class Relation {
	// Rather than a 2D array of booleans, the matrix is instead an array of BitArray objects. BitArray is
	// effectively a variable-length array of booleans, but stores values as bits of a 32-bit integer.
	// This is because Java needs to use a byte to store a boolean value (sometimes more, depending on the
	// implementation), so for large arrays of booleans memory usage increases dramatically.
	private BitArray[] m;
	// After calculating if the relation is reflexive/symmetric/antisymmetric/transitive, the relation
	// remembers the results of that check to save processing time if that property is checked again
	// before the relation is modified. If the relation is modified, it resets these fields to null.
	private Boolean isReflexive;
	private Boolean isSymmetric;
	private Boolean isAntisymmetric;
	private Boolean isTransitive;
	// Similarly, it will also remember the equivalence classes and transitive closure after they are
	// computed, so long as the relation is not changed.
	private Integer[][] equivClasses;
	private Relation transitiveClosure;
	
	// Default constructor
	public Relation(int nElements) {
		m = new BitArray[nElements];
		for(int i = 0; i < m.length; i++) {
			m[i] = new BitArray(nElements);
		}
	}
	// Copy constructor, initializes itself with the same values as the given relation.
	public Relation(Relation r) {
		m = new BitArray[r.m.length];
		for(int i = 0; i < m.length; i++) {
			m[i] = new BitArray(r.m[i]);
		}
	}
	
	// Returns true if the relation is reflexive.
	public boolean isReflexive() {
		// If reflexiveness has already been checked, return the previous result.
		if(isReflexive != null)
			return isReflexive;
		// Check reflexiveness by checking the diagonal entries of the matrix.
		isReflexive = false;
		for(int i = 0; i < m.length; i++) {
			if(!m[i].get(i))
				return false;
		}
		isReflexive = true;
		return true;
	}
	
	// Returns true if the relation is symmetric.
	public boolean isSymmetric() {
		// If symmetry has not been checked, run the method to check it.
		if(isSymmetric == null)
			updateSymmetry();
		return isSymmetric;
	}
	
	// Returns true if the relation is antisymmetric.
	public boolean isAntisymmetric() {
		// If symmetry has not been checked, run the method to check it.
		if(isAntisymmetric == null)
			updateSymmetry();
		return isAntisymmetric;
	}
	
	// This is a helper method to update the values of isSymmetric and isAntisymmetric. The algorithm used
	// to check either one is very similar, so it is more convenient to check them both at the same time.
	private void updateSymmetry() {
		isSymmetric = true;
		isAntisymmetric = true;
		for(int i = 0; i < m.length; i++) {
			for(int j = 0; j <= i; j++) {
				if(i != j) {
					boolean b = m[i].get(j);
					if(b ^ m[j].get(i)) {
						isSymmetric = false;
						if(!isAntisymmetric)
							return;
					} else if(b) {
						isAntisymmetric = false;
						if(!isSymmetric)
							return;
					}
				}
			}
		}
	}
	
	// Returns true if the relation is transitive.
	public boolean isTransitive() {
		if(isTransitive != null)
			return isTransitive;
		transitiveClosure();
		return isTransitive;
	}
	
	// Returns true if the relation is an equivalence matrix.
	public boolean isEquivalence() {
		// A relation is an equivalence matrix if and only if it is reflexive, symmetric, and transitive.
		return isReflexive() && isSymmetric() && isTransitive();
	}
	
	// Returns true if the ath element relates the bth element.
	public boolean relates(int a, int b) {
		if(a >= m.length || b >= m.length)
			return false;
		return m[a].get(b);
	}
	
	// Sets the relation such that the ath element relates the bth element.
	public void set(int a, int b, boolean value) {
		resetProperties();
		if(a >= m.length || b >= m.length)
			return;
		m[a].set(b, value);
	}
	
	// Returns a relation that is the transitive closure of this relation.
	public Relation transitiveClosure() {
		if(transitiveClosure != null)
			return transitiveClosure;
		transitiveClosure = new Relation(this);
		if(isTransitive != null && isTransitive)
			return transitiveClosure;
		isTransitive = transitiveClosure.applyWarshall();
		return transitiveClosure;
	}
	
	// This method applies Warshall's algorithm to this matrix.
	// It does so by making a list of every column containing a one in the ith row and a list of every row
	// containing a one in the ith column. It then sets every (row, column) pair from those lists to one.
	// It repeats this for every value of i, where 0 < i < # of elements.
	// This method returns true if no changes are made.
	private boolean applyWarshall() {
		boolean wasTransitive = true;
		ArrayList<Integer> rows = new ArrayList<>();
		ArrayList<Integer> columns = new ArrayList<>();
		
		for(int i = 0; i < m.length; i++) {
			for(int j = 0; j < m.length; j++) {
				if(m[i].get(j))
					columns.add(j);
				if(m[j].get(i))
					rows.add(j);
			}
			for(int row : rows) {
				for(int column : columns) {
					if(!m[row].get(column)) {
						wasTransitive = false;
						m[row].set(column, true);
					}
				}
			}
			rows.clear();
			columns.clear();
		}
		
		isTransitive = true;
		return wasTransitive;
	}
	
	// Returns every equivalence class of the given relation. If the relation is not an equivalence
	// matrix, this returns null.
	public Integer[][] equivClasses() {
		if(equivClasses != null)
			return equivClasses;
		if(!isEquivalence())
			return null;
		
		// List of equivalence classes that have been found.
		ArrayList<Integer[]> classesList = new ArrayList<>();
		// List of values who have already had their equivalences checked.
		ArrayList<Integer> foundValues = new ArrayList<>();
		
		for(int i = 0; i < m.length; i++) {
			if(!foundValues.contains(i)) {
				// Initialize an ArrayList to store this new class, then fill it with all matching
				// elements.
				ArrayList<Integer> ar = new ArrayList<>();
				ar.add(i);
				findEquivalences(i, ar);
				// Convert the class to an array of Integers, then add it to the list of classes.
				classesList.add(ar.toArray(new Integer[ar.size()]));
				// Add every value from the class into the list of found values.
				foundValues.addAll(ar);
			}
		}
		// Convert the list of classes to an array.
		equivClasses = classesList.toArray(new Integer[classesList.size()][]);
		
		return equivClasses;
	}
	
	// This method finds every one in the nth column of the matrix, and adds its column to the list of
	// values that relate to the nth element.
	private void findEquivalences(int n, ArrayList<Integer> ar) {
		for(int i = n; i < m.length; i++) {
			if(m[n].get(i)) {
				// Check if the list already contains i, and if not, add it to the list and find all
				// values related to i.
				if(!ar.contains(i)) {
					ar.add(i);
					findEquivalences(i, ar);
				}
			}
		}
	}
	
	// Convert the equivalence classes to a string for convenient printing.
	public String equivClassesAsString() {
		// Make sure the equivalence classes is initialized, and that it is, indeed, an equivalence
		// relation.
		equivClasses();
		if(equivClasses == null)
			return "(n/a)";
		
		String s = "{";
		
		for(int i = 0; i < equivClasses.length; i++) {
			s += "{" + (equivClasses[i][0] + 1);
			for(int j = 1; j < equivClasses[i].length; j++) {
				s += "," + (equivClasses[i][j] + 1);
			}
			s += "}";
			if(equivClasses.length - i > 1)
				s += ",";
		}
		
		return s + "}";
	}
	
	// Reset the properties to null, so their respective methods know they need to be rechecked.
	private void resetProperties() {
		isReflexive = null;
		isSymmetric = null;
		isAntisymmetric = null;
		isTransitive = null;
		equivClasses = null;
		transitiveClosure = null;
	}
	
	// Return the number of elements in the relation.
	public int nElements() {
		return m.length;
	}
	
	// Returns the relation's matrix as a string, using only ASCII characters (for compatibility).
	private String toStringAscii() {
		String s = " _";
		String whitespace = " ";
		for(int i = 0; i < m.length; i++) {
			whitespace += "  ";
		}
		s += whitespace + "_ ";
		
		for(int i = 0; i < m.length; i++) {
			if(i + 1 == m.length) {
				s += "\n|_ ";
			} else {
				s += "\n|  ";
			}
			for(int j = 0; j < m.length; j++) {
				s += m[i].get(j) ? "1" : "0";
				s += " ";
			}
			if(i + 1 == m.length) {
				s += "_|";
			} else {
				s += " |";
			}
			
		}
		
		return s;
	}
	
	// Returns the relation's matrix as a string, making use of Unicode box-drawing characters.
	private String toStringUnicode() {
		String s = "┌";
		String whitespace = "   ";
		for(int i = 0; i < m.length; i++) {
			whitespace += "  ";
		}
		s += whitespace + "┐";
		
		for(int i = 0; i < m.length; i++) {
			s += "\n│  ";
			for(int j = 0; j < m.length; j++) {
				s += m[i].get(j) ? "1" : "0";
				s += " ";
			}
			s += " │";
		}
		
		s += "\n└" + whitespace + "┘";
		
		return s;
	}
	
	// Default toString method (uses Unicode by default).
	public String toString() {
		return toStringUnicode();
	}
	
	// Convert to string, with the given text format.
	public String toString(boolean useUnicode) {
		if(useUnicode)
			return toStringUnicode();
		return toStringAscii();
	}
	
	// This class is a way to store an array of boolean values in a more memory-efficient format, using
	// bits of 32-bit integers to store data.
	private class BitArray {
		private int length;
		private int[] data;
		
		// Constructor
		private BitArray(int length) {
			this.length = length;
			data = new int[length/32 + (length%32 == 0 ? 0 : 1)];
		}
		// Copy constructor
		private BitArray(BitArray b) {
			length = b.length;
			data = b.data.clone();
		}
		
		// Get the ith boolean of the array
		private boolean get(int i) {
			if(i >= length)
				return false;
			return (data[i/32] & (1 << (i%32))) != 0;
		}
		
		// Set the ith boolean of the array to the given value
		private boolean set(int i, boolean val) {
			if(i >= length)
				return false;
			int n = 1 << (i%32);
			if(val) {
				data[i/32] = data[i/32] | n;
			} else {
				data[i/32] = data[i/32] & ~n;
			}
			return true;
		}
	}
}
