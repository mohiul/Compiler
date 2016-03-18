package smbl;

public class VariableType {
	private VariableTypeName typeName;
	private int dimension[];
	
	public VariableTypeName getTypeName() {
		return typeName;
	}
	public void setTypeName(VariableTypeName typeName) {
		this.typeName = typeName;
	}
	public int[] getDimension() {
		return dimension;
	}
	public void setDimension(int[] dimension) {
		this.dimension = dimension;
	}
	
	
}
