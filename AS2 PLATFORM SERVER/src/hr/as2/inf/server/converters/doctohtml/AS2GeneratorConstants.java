package hr.as2.inf.server.converters.doctohtml;

public class AS2GeneratorConstants {

	static public final String PREFIX = "msw:"; // microsoft word
	static public final String URI = "http://www.apache.org/cocoon/poi/word/v1.0";
	static public final String DOCUMENT_ELEMENT = "document";
	static public final String BODY_ELEMENT = "body";
	static public final String SECTION_ELEMENT = "section";
	static public final String TITLE_ELEMENT = "title";
	static public final String TABLE_ELEMENT = "table";
	static public final String TABLE_TR_ELEMENT = "tr";
	static public final String TABLE_TD_ELEMENT = "td";
	static public final String PARAGRAPH_ELEMENT = "p";
	static public final String ITALIC_ELEMENT = "i";
	static public final String BOLD_ELEMENT = "b";
	static public final String NEWLINE_ELEMENT = "br";
	static public final String LIST_ELEMENT = "ul";
	static public final String LISTNUM_ELEMENT = "ol";
	static public final String ITEM_ELEMENT = "li";
	static public final String RIGHT_ELEMENT = "right";
	static public final String CENTER_ELEMENT = "center";
	static public final String HEADER_1 = "h1";
	static public final String HEADER_2 = "h2";
	static public final String HEADER_3 = "h3";
	static public final String HEADER_4 = "h4";
	static public final String HEADER_5 = "h5";
	static public final String HEADER_6 = "h6";
	static public final String HEADER_7 = "h7";
	static public final String HEADER_8 = "h8";
	static public final String HEADER_9 = "h9";
	static public final String HEADER_10 = "h10";

	static final int NORMAL_PARAGRAPH = 0;
	static final int BULLET_PARAGRAPH = 5;
	static final int NUMERED_PARAGRAPH = 6;
	int list_type;

	static final int LEFT_ALIGN = 0;
	static final int CENTER_ALIGN = 1;
	static final int RIGHT_ALIGN = 2;


	static final int stiNormalPara 			= 0;	// 0x0000
	static final int stiHeading1    		= 1;	// 0x0001
	static final int stiHeading2    		= 2;	// 0x0002
	static final int stiHeading3    		= 3;	// 0x0003
	static final int stiHeading4    		= 4;	// 0x0004
	static final int stiHeading5    		= 5;	// 0x0005
	static final int stiHeading6    		= 6;	// 0x0006
	static final int stiHeading7    		= 7;	// 0x0007
	static final int stiHeading8    		= 8;	// 0x0008
	static final int stiHeading9    		= 9;	// 0x0009
	//static final int stiHeadingFirst stiHeading1
	//static final int stiHeadingLast stiHeading9
	static final int stiIndex1      		= 10;	// 0x000A
	static final int stiIndex2      		= 11;	// 0x000B
	static final int stiIndex3      		= 12;	// 0x000C
	static final int stiIndex4      		= 13;	// 0x000D
	static final int stiIndex5      		= 14;	// 0x000E
	static final int stiIndex6      		= 15;	// 0x000F
	static final int stiIndex7      		= 16;	// 0x0010
	static final int stiIndex8      		= 17;	// 0x0011
	static final int stiIndex9      		= 18;	// 0x0012
	//static final int stiIndexFirst  stiIndex1
	//static final int stiIndexLast   stiIndex9
	static final int stiToc1        		= 19;	// 0x0013
	static final int stiToc2        		= 20;	// 0x0014
	static final int stiToc3        		= 21;	// 0x0015
	static final int stiToc4        		= 22;	// 0x0016
	static final int stiToc5        		= 23;	// 0x0017
	static final int stiToc6        		= 24;	// 0x0018
	static final int stiToc7        		= 25;	// 0x0019
	static final int stiToc8        		= 26;	// 0x001A
	static final int stiToc9        		= 27;	// 0x001B
	//static final int stiTocFirst    stiToc1
	//static final int stiTocLast     stiToc9
	static final int stiNormIndent 			= 28;	// 0x001C
	static final int stiFtnText				= 29;	// 0x001D
	static final int stiAtnText         	= 30;	// 0x001E
	static final int stiHeader          	= 31;	// 0x001F
	static final int stiFooter          	= 32;	// 0x0020
	static final int stiIndexHeading		= 33;	// 0x0021
	static final int stiCaption         	= 34;	// 0x0022
	static final int stiToCaption       	= 35;	// 0x0023
	static final int stiEnvAddr         	= 36;	// 0x0024
	static final int stiEnvRet          	= 37;	// 0x0025
	static final int stiFtnRef          	= 38;	// 0x0026       char    style
	static final int stiAtnRef          	= 39;	// 0x0027       char    style
	static final int stiLnn             	= 40;	// 0x0028       char    style
	static final int stiPgn             	= 41;	// 0x0029       char    style
	static final int stiEdnRef          	= 42;	// 0x002A       char    style
	static final int stiEdnText         	= 43;	// 0x002B
	static final int stiToa             	= 44;	// 0x002C
	static final int stiMacro           	= 45;	// 0x002D
	static final int stiToaHeading			= 46;	// 0x002E
	static final int stiList           		= 47;	// 0x002F
	static final int stiListBullet 			= 48;	// 0x0030
	static final int stiListNumber 			= 49;	// 0x0031
	static final int stiList2           	= 50;	// 0x0032
	static final int stiList3           	= 51;	// 0x0033
	static final int stiList4           	= 52;	// 0x0034
	static final int stiList5           	= 53;	// 0x0035
	static final int stiListBullet2 		= 54;	// 0x0036
	static final int stiListBullet3 		= 55;	// 0x0037
	static final int stiListBullet4 		= 56;	// 0x0038
	static final int stiListBullet5 		= 57;	// 0x0039
	static final int stiListNumber2 		= 58;	// 0x003A
	static final int stiListNumber3 		= 59;	// 0x003B
	static final int stiListNumber4 		= 60;	// 0x003C
	static final int stiListNumber5 		= 61;	// 0x003D
	static final int stiTitle          		= 62;	// 0x003E
	static final int stiClosing         	= 63;	// 0x003F
	static final int stiSignature       	= 64;	// 0x0040
	static final int stiNormalChar 			= 65;	// 0x0041       char style
	static final int stiBodyText        	= 66;	// 0x0042
	static final int stiBodyTextInd 		= 67;	// 0x0043
	static final int stiListCont        	= 68;	// 0x0044
	static final int stiListCont2       	= 69;	// 0x0045
	static final int stiListCont3       	= 70;	// 0x0046
	static final int stiListCont4       	= 71;	// 0x0047
	static final int stiListCont5       	= 72;	// 0x0048
	static final int stiMsgHeader       	= 73;	// 0x0049
	static final int stiSubtitle        	= 74;	// 0x004A
	static final int stiSalutation			= 75;	// 0x004B
	static final int stiDate            	= 76;	// 0x004C
	static final int stiBodyText1I 			= 77;	// 0x004D
	static final int stiBodyText1I2 		= 78;	// 0x004E
	static final int stiNoteHeading 		= 79;	// 0x004F
	static final int stiBodyText2       	= 80;	// 0x0050
	static final int stiBodyText3       	= 81;	// 0x0051
	static final int stiBodyTextInd2 		= 82;	// 0x0052
	static final int stiBodyTextInd3 		= 83;	// 0x0053
	static final int stiBlockQuote 			= 84;	// 0x0054
	static final int stiHyperlink      		= 85;	// 0x0055 char          style
	static final int stiHyperlinkFollowed 	= 86;	// 0x0056                    char style
	static final int stiStrong          	= 87;	// 0x0057 char          style
	static final int stiEmphasis        	= 88;	// 0x0058 char          style
	static final int stiNavPane         	= 89;	// 0x0059 char          style
	static final int stiPlainText       	= 90;	// 0x005A
	static final int stiAutoSig         	= 91;	// 0x005B
	static final int stiFormTop         	= 92;	// 0x005C
	static final int stiFormBottom 			= 93;	// 0x005D
	static final int stiHtmlNormal 			= 94;	// 0x005E
	static final int stiHtmlAcronym 		= 95;	// 0x005F char          style
	static final int stiHtmlAddress 		= 96;	// 0x0060
	static final int stiHtmlCite        	= 97;	// 0x0061 char          style
	static final int stiHtmlCode        	= 98;	// 0x0062 char          style
	static final int stiHtmlDfn         	= 99;	// 0x0063 char          style
	static final int stiHtmlKbd         	= 100;	// 0x0064 char          style
	static final int stiHtmlPre         	= 101;	// 0x0065
	static final int stiHtmlSamp        	= 102;	// 0x0066 char          style
	static final int stiHtmlTt          	= 103;	// 0x0067 char          style
	static final int stiHtmlVar         	= 104;	// 0x0068 char          style
	static final int stiNormalTable 		= 105;	// 0x0069 table                   style
	static final int stiAtnSubject 			= 106;	// 0x0070

	// The following Table and List styles were added in Word 2002:
	static final int stiNormalList 			= 107;	// 0x0071 list style
	static final int stiOutlineList1 		= 108;	// 0x0072 list style (1 / a / i)
	static final int stiOutlineList2 		= 109;	// 0x0073 list style (1 / 1.1 / 1.1.1)
	static final int stiOutlineList3 		= 110;	// 0x0074 list style (Article / Section)
	//static final int stiListStyleFirst stiNormalList                  // First default list style
	//static final int stiListStyleLast stiOutlineList3 // Last default list style
	static final int stiTableSimple1 		= 111;
	static final int stiTableSimple2 		= 112;
	static final int stiTableSimple3 		= 113;
	static final int stiTableClassic1 		= 114;
	static final int stiTableClassic2 		= 115;
	static final int stiTableClassic3 		= 116;
	static final int stiTableClassic4 		= 117;
	static final int stiTableColorful1 		= 118;
	static final int stiTableColorful2 		= 119;
	static final int stiTableColorful3 		= 120;
	static final int stiTableColumns1 		= 121;
	static final int stiTableColumns2 		= 122;
	static final int stiTableColumns3 		= 123;
	static final int stiTableColumns4 		= 124;
	static final int stiTableColumns5 		= 125;
	static final int stiTableGrid1 			= 126;
	static final int stiTableGrid2 			= 127;
	static final int stiTableGrid3 			= 128;
	static final int stiTableGrid4 			= 129;
	static final int stiTableGrid5 			= 130;
	static final int stiTableGrid6 			= 131;
	static final int stiTableGrid7 			= 132;
	static final int stiTableGrid8 			= 133;
	static final int stiTableList1 			= 134;
	static final int stiTableList2 			= 135;
	static final int stiTableList3 			= 136;
	static final int stiTableList4 			= 137;
	static final int stiTableList5 			= 138;
	static final int stiTableList6 			= 139;
	static final int stiTableList7 			= 140;
	static final int stiTableList8 			= 141;
	static final int stiTable3DFx1 			= 142;
	static final int stiTable3DFx2 			= 143;
	static final int stiTable3DFx3 			= 144;
	static final int stiTableContemporary 	= 145;
	static final int stiTableElegant 		= 146;
	static final int stiTableProfessional 	= 147;
	static final int stiTableSubtle1 		= 148;
	static final int stiTableSubtle2 		= 149;
	static final int stiTableWeb1 			= 150;
	static final int stiTableWeb2 			= 151;
	static final int stiTableWeb3 			= 152;
	//static final int stiTableFirst                  stiTableSimple1
	//static final int stiTableLast                   stiTableWeb3
	static final int stiAcetate 			= 153;
	static final int stiTableGrid 			= 154;
	static final int stiTableTheme 			= 155;
	static final int stiMax 				= 156;	//   number    of  defined     sti's
	static final int stiChpMax 				= 19;	//   number    of  defined     char style sti's
	static final int stiPapMax 				= 87;	//   number    of  defined     para style sti's
	static final int stiTableMax 			= 1;	//   number    of  defined     table style sti's


}