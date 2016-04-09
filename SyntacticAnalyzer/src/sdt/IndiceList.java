package sdt;

public class IndiceList {
	public Indice indice;
	public IndiceList indiceList;
	
	public int getNoOfDim() {
		int noOfDim = 0;
		Indice indice1 = indice;
		IndiceList indiceList1 = indiceList;
	
		while(indice1 != null){
			if(indice1.arithExpr != null) noOfDim++;
			if(indiceList1 != null){
				indice1 = indiceList1.indice;				
				indiceList1 = indiceList1.indiceList;
			} else {
				indice1 = null;
			}
		}
		return noOfDim;
	}
}
