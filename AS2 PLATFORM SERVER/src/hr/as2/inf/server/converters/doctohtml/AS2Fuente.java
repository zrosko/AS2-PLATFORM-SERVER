package hr.as2.inf.server.converters.doctohtml;

import java.util.ArrayList;
import java.util.List;


public class AS2Fuente {

	private String family;
	private int size;
	private boolean bold;
	private boolean italic;

	public AS2Fuente() {
		super();
	}

	public AS2Fuente(String family, int size, boolean bold, boolean italic) {
		super();
		this.family = family;
		this.size = size;
		this.bold = bold;
		this.italic = italic;
	}

	public String getFamily() {
		return family;
	}
	public void setFamily(String family) {
		this.family = family;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public boolean isItalic() {
		return italic;
	}

	public void setItalic(boolean italic) {
		this.italic = italic;
	}

	public boolean isBold() {
		return bold;
	}

	public void setBold(boolean bold) {
		this.bold = bold;
	}

	public static void main(String[] args) {
		List<Integer> listaValores = new ArrayList<Integer>();
		listaValores.add(22);
		listaValores.add(23);
		listaValores.add(24);
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(" ( ");
		for(int i = 0; i<listaValores.size(); i++){
			if(i != 0 && i != listaValores.size()){
				stringBuffer.append(" OR ");
			}
			stringBuffer.append("empresa.tipo_entidad");
			stringBuffer.append(" = ");
			stringBuffer.append(listaValores.get(i));
		}
		stringBuffer.append(" ) ");
		System.out.println(stringBuffer);
	}
}
