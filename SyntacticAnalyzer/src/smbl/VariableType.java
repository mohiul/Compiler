package smbl;


public class VariableType {
	private String typeName;
	private int dimension[];
	
	public String getTypeName() {
		return typeName;
	}
	
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	
	public int[] getDimension() {
		return dimension;
	}
	
	public void setDimension(int[] dimension) {
		this.dimension = dimension;
	}
	
	@Override
	public String toString() {
		String str = typeName;
		if(dimension != null){
			for(int dim: dimension){
				str += "[" + dim + "]";
			}
		}
		return str;
	}
}
