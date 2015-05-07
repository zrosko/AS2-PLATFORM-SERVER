package hr.as2.inf.server.converters.doctohtml;

import org.apache.poi.hwpf.model.StyleDescription;
import org.apache.poi.hwpf.model.StyleSheet;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Picture;


public class AS2MSWordStyle {

	public static String pintaLista(String item){
    	return "<ul><li>" + item + "</li></ul>";
    }

	public static String pintaParrafo(Paragraph p, StyleSheet styleSheet, AS2Fuente fuente, String texto_a_pintar){
		StyleDescription paragraphStyle = styleSheet.getStyleDescription(p.getStyleIndex());
        String styleName = paragraphStyle.getName();
        //System.out.println("styleName="+styleName);

        if(p.getStyleIndex() == AS2GeneratorConstants.stiHeading1){
        	return "<p><h1 " + devuelveEstilo(p, fuente) + ">" + getFormatedText(texto_a_pintar) + "</h1></p>";
        }else if(p.getStyleIndex() == AS2GeneratorConstants.stiHeading2){
        	return "<p><h2 " + devuelveEstilo(p, fuente) + ">" + getFormatedText(texto_a_pintar) + "</h2></p>";
        }else if(p.getStyleIndex() == AS2GeneratorConstants.stiHeading3){
        	return "<p><h3 " + devuelveEstilo(p, fuente) + ">" + getFormatedText(texto_a_pintar) + "</h3></p>";
        }else if(p.getStyleIndex() == AS2GeneratorConstants.stiHeading4){
        	return "<p><h4 " + devuelveEstilo(p, fuente) + ">" + getFormatedText(texto_a_pintar) + "</h4></p>";
        }else if(p.getStyleIndex() == AS2GeneratorConstants.stiHeading5){
        	return "<p><h5 " + devuelveEstilo(p, fuente) + ">" + getFormatedText(texto_a_pintar) + "</h5></p>";
        }else if(p.getStyleIndex() == AS2GeneratorConstants.stiHeading6){
        	return "<p><h6 " + devuelveEstilo(p, fuente) + ">" + getFormatedText(texto_a_pintar) + "</h6></p>";
        }else if(p.getStyleIndex() == AS2GeneratorConstants.stiHeading7){
        	return "<p><h7 " + devuelveEstilo(p, fuente) + ">" + getFormatedText(texto_a_pintar) + "</h7></p>";
        }else if(p.getStyleIndex() == AS2GeneratorConstants.stiHeading8){
        	return "<p><h8 " + devuelveEstilo(p, fuente) + ">" + getFormatedText(texto_a_pintar) + "</h8></p>";
        }else if(p.getStyleIndex() == AS2GeneratorConstants.stiHeading9){
        	return "<p><h9 " + devuelveEstilo(p, fuente) + ">" + getFormatedText(texto_a_pintar) + "</h9></p>";
        }else if(styleName.contains("Bullet")){
        	return "<li " + devuelveEstilo(p, fuente) + ">" + texto_a_pintar + "</li>";
        }else if(p.getStyleIndex() == AS2GeneratorConstants.stiHyperlink){
        	return "<a href='#'>" + texto_a_pintar + "</a>";
        }else if(styleName.contains("Normal")){
        	return texto_a_pintar;
        }else{
        	return texto_a_pintar;
        }
    }
	@SuppressWarnings("deprecation")
	public static String pintaImagen(Picture pic, String path, String pictureFullFileName,String fileExtension, String byteArray){
		int _width = (pic.getWidth() * pic.getAspectRatioX())/100;
		int _height = (pic.getHeight() * pic.getAspectRatioY())/100;
		return "<img src=\"data:image/"+fileExtension+";base64,"+byteArray+ "\" width=\"" + _width + "\" height = \"" + _height
    	+ "\" border=\"0\" alt=\"" + pictureFullFileName + "\">";
//    	return "<img src=\""+path +
//    			pictureFullFileName + "\" width=\"" + _width + "\" height = \"" + _height
//    	+ "\" border=\"0\" alt=\"" + pictureFullFileName + "\">";
    }

    private static String devuelveEstilo(Paragraph p, AS2Fuente fuente){
    	String estilo = " style =\"";
    	//int fontAlignment = p.getEndOffset();
    	//int fontAlignment = p.getIndentFromLeft();
    	//int fontAlignment = p.getSpacingAfter();
    	//int fontAlignment = p.getSpacingBefore();
    	//int fontAlignment = p.getStartOffset();
    	int fontAlignment = p.getStyleIndex();

    	if(fontAlignment!=0){
    	//System.out.println(fontAlignment + "   " + new String(p.text()));
    	}

    	// Ponemos estilo de alineaciĂłn del parrafo.
    	estilo += getJustification(p);
    	// Ponemos las tabulaciones
    	estilo += getFirstLineIndent(p);
    	// Ponemos el estilo de la fuente
    	estilo += getFontStyle(fuente);

    	estilo += "\"";
    	return estilo;
    }

    private static String getFirstLineIndent(Paragraph p){
    	return "text-indent:" + p.getFirstLineIndent() + ";";
    }

    private static String getJustification(Paragraph p){
    	int justification = p.getJustification();
    	//System.out.println(justification + "   " + new String(p.text()));
		switch (justification){
    		case 0 :
				return "text-align: left;";
			case 1 :
				return "text-align: center;";
			case 2 :
				return "text-align: right;";
			case 3 :
				return "text-align: justify;";
			default :
				return "text-align: left;";
		}
    }

    private static String getFontStyle(AS2Fuente fuente){
    	String estilo = "";
    	if(fuente.getFamily() != null && !fuente.getFamily().equals(""))
    		estilo += "font-family:" + fuente.getFamily() + ";";
    	estilo += "font-size:" + fuente.getSize() + ";";
    	if(fuente.isBold())
    		estilo += "font-weight:bold;";
    	if(fuente.isItalic())
    		estilo += "font-style:italic;";
    	return estilo;
    }

    private static String getFormatedText(String text){
    	// Fix line endings (Note - won't get all of them
    	text = text.replaceAll("\r\r\r", "\r\n\r\n\r\n");
    	text = text.replaceAll("\r\r", "\r\n\r\n");

    	if(text.endsWith("\r")) {
    		text += "\n";
    	}
    	return text;
    }
}
