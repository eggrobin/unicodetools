/**
 *******************************************************************************
 * Copyright (C) 1996-2001, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /home/cvsroot/unicodetools/org/unicode/text/UCA/Case.java,v $
 *
 *******************************************************************************
 */

package org.unicode.text.UCA;

public final class Case {

	static StringBuffer out = new StringBuffer();

	static String fold(char c) {
		return fold(String.valueOf(c));
	}

	static String fold(String in) {
		synchronized (out) {
			out.setLength(0);
			for (int i = 0; i < in.length(); ++i) {
				final char c = in.charAt(i);
				final String f = CF[c];
				if (f == null) {
					out.append(c);
				} else {
					out.append(f);
				}
			}
			return out.toString();
		}
	}

	static String[] CF = new String[65536];
	static {
		CF[0x0041]="\u0061";
		CF[0x0042]="\u0062";
		CF[0x0043]="\u0063";
		CF[0x0044]="\u0064";
		CF[0x0045]="\u0065";
		CF[0x0046]="\u0066";
		CF[0x0047]="\u0067";
		CF[0x0048]="\u0068";
		CF[0x0049]="\u0069";
		CF[0x004A]="\u006A";
		CF[0x004B]="\u006B";
		CF[0x004C]="\u006C";
		CF[0x004D]="\u006D";
		CF[0x004E]="\u006E";
		CF[0x004F]="\u006F";
		CF[0x0050]="\u0070";
		CF[0x0051]="\u0071";
		CF[0x0052]="\u0072";
		CF[0x0053]="\u0073";
		CF[0x0054]="\u0074";
		CF[0x0055]="\u0075";
		CF[0x0056]="\u0076";
		CF[0x0057]="\u0077";
		CF[0x0058]="\u0078";
		CF[0x0059]="\u0079";
		CF[0x005A]="\u007A";
		CF[0x00B5]="\u03BC";
		CF[0x00C0]="\u00E0";
		CF[0x00C1]="\u00E1";
		CF[0x00C2]="\u00E2";
		CF[0x00C3]="\u00E3";
		CF[0x00C4]="\u00E4";
		CF[0x00C5]="\u00E5";
		CF[0x00C6]="\u00E6";
		CF[0x00C7]="\u00E7";
		CF[0x00C8]="\u00E8";
		CF[0x00C9]="\u00E9";
		CF[0x00CA]="\u00EA";
		CF[0x00CB]="\u00EB";
		CF[0x00CC]="\u00EC";
		CF[0x00CD]="\u00ED";
		CF[0x00CE]="\u00EE";
		CF[0x00CF]="\u00EF";
		CF[0x00D0]="\u00F0";
		CF[0x00D1]="\u00F1";
		CF[0x00D2]="\u00F2";
		CF[0x00D3]="\u00F3";
		CF[0x00D4]="\u00F4";
		CF[0x00D5]="\u00F5";
		CF[0x00D6]="\u00F6";
		CF[0x00D8]="\u00F8";
		CF[0x00D9]="\u00F9";
		CF[0x00DA]="\u00FA";
		CF[0x00DB]="\u00FB";
		CF[0x00DC]="\u00FC";
		CF[0x00DD]="\u00FD";
		CF[0x00DE]="\u00FE";
		CF[0x00DF]="\u0073\u0073";
		CF[0x0100]="\u0101";
		CF[0x0102]="\u0103";
		CF[0x0104]="\u0105";
		CF[0x0106]="\u0107";
		CF[0x0108]="\u0109";
		CF[0x010A]="\u010B";
		CF[0x010C]="\u010D";
		CF[0x010E]="\u010F";
		CF[0x0110]="\u0111";
		CF[0x0112]="\u0113";
		CF[0x0114]="\u0115";
		CF[0x0116]="\u0117";
		CF[0x0118]="\u0119";
		CF[0x011A]="\u011B";
		CF[0x011C]="\u011D";
		CF[0x011E]="\u011F";
		CF[0x0120]="\u0121";
		CF[0x0122]="\u0123";
		CF[0x0124]="\u0125";
		CF[0x0126]="\u0127";
		CF[0x0128]="\u0129";
		CF[0x012A]="\u012B";
		CF[0x012C]="\u012D";
		CF[0x012E]="\u012F";
		CF[0x0130]="\u0069";
		CF[0x0131]="\u0069";
		CF[0x0132]="\u0133";
		CF[0x0134]="\u0135";
		CF[0x0136]="\u0137";
		CF[0x0139]="\u013A";
		CF[0x013B]="\u013C";
		CF[0x013D]="\u013E";
		CF[0x013F]="\u0140";
		CF[0x0141]="\u0142";
		CF[0x0143]="\u0144";
		CF[0x0145]="\u0146";
		CF[0x0147]="\u0148";
		CF[0x0149]="\u02BC\u006E";
		CF[0x014A]="\u014B";
		CF[0x014C]="\u014D";
		CF[0x014E]="\u014F";
		CF[0x0150]="\u0151";
		CF[0x0152]="\u0153";
		CF[0x0154]="\u0155";
		CF[0x0156]="\u0157";
		CF[0x0158]="\u0159";
		CF[0x015A]="\u015B";
		CF[0x015C]="\u015D";
		CF[0x015E]="\u015F";
		CF[0x0160]="\u0161";
		CF[0x0162]="\u0163";
		CF[0x0164]="\u0165";
		CF[0x0166]="\u0167";
		CF[0x0168]="\u0169";
		CF[0x016A]="\u016B";
		CF[0x016C]="\u016D";
		CF[0x016E]="\u016F";
		CF[0x0170]="\u0171";
		CF[0x0172]="\u0173";
		CF[0x0174]="\u0175";
		CF[0x0176]="\u0177";
		CF[0x0178]="\u00FF";
		CF[0x0179]="\u017A";
		CF[0x017B]="\u017C";
		CF[0x017D]="\u017E";
		CF[0x017F]="\u0073";
		CF[0x0181]="\u0253";
		CF[0x0182]="\u0183";
		CF[0x0184]="\u0185";
		CF[0x0186]="\u0254";
		CF[0x0187]="\u0188";
		CF[0x0189]="\u0256";
		CF[0x018A]="\u0257";
		CF[0x018B]="\u018C";
		CF[0x018E]="\u01DD";
		CF[0x018F]="\u0259";
		CF[0x0190]="\u025B";
		CF[0x0191]="\u0192";
		CF[0x0193]="\u0260";
		CF[0x0194]="\u0263";
		CF[0x0196]="\u0269";
		CF[0x0197]="\u0268";
		CF[0x0198]="\u0199";
		CF[0x019C]="\u026F";
		CF[0x019D]="\u0272";
		CF[0x019F]="\u0275";
		CF[0x01A0]="\u01A1";
		CF[0x01A2]="\u01A3";
		CF[0x01A4]="\u01A5";
		CF[0x01A6]="\u0280";
		CF[0x01A7]="\u01A8";
		CF[0x01A9]="\u0283";
		CF[0x01AC]="\u01AD";
		CF[0x01AE]="\u0288";
		CF[0x01AF]="\u01B0";
		CF[0x01B1]="\u028A";
		CF[0x01B2]="\u028B";
		CF[0x01B3]="\u01B4";
		CF[0x01B5]="\u01B6";
		CF[0x01B7]="\u0292";
		CF[0x01B8]="\u01B9";
		CF[0x01BC]="\u01BD";
		CF[0x01C4]="\u01C6";
		CF[0x01C5]="\u01C6";
		CF[0x01C7]="\u01C9";
		CF[0x01C8]="\u01C9";
		CF[0x01CA]="\u01CC";
		CF[0x01CB]="\u01CC";
		CF[0x01CD]="\u01CE";
		CF[0x01CF]="\u01D0";
		CF[0x01D1]="\u01D2";
		CF[0x01D3]="\u01D4";
		CF[0x01D5]="\u01D6";
		CF[0x01D7]="\u01D8";
		CF[0x01D9]="\u01DA";
		CF[0x01DB]="\u01DC";
		CF[0x01DE]="\u01DF";
		CF[0x01E0]="\u01E1";
		CF[0x01E2]="\u01E3";
		CF[0x01E4]="\u01E5";
		CF[0x01E6]="\u01E7";
		CF[0x01E8]="\u01E9";
		CF[0x01EA]="\u01EB";
		CF[0x01EC]="\u01ED";
		CF[0x01EE]="\u01EF";
		CF[0x01F0]="\u006A\u030C";
		CF[0x01F1]="\u01F3";
		CF[0x01F2]="\u01F3";
		CF[0x01F4]="\u01F5";
		CF[0x01F6]="\u0195";
		CF[0x01F7]="\u01BF";
		CF[0x01F8]="\u01F9";
		CF[0x01FA]="\u01FB";
		CF[0x01FC]="\u01FD";
		CF[0x01FE]="\u01FF";
		CF[0x0200]="\u0201";
		CF[0x0202]="\u0203";
		CF[0x0204]="\u0205";
		CF[0x0206]="\u0207";
		CF[0x0208]="\u0209";
		CF[0x020A]="\u020B";
		CF[0x020C]="\u020D";
		CF[0x020E]="\u020F";
		CF[0x0210]="\u0211";
		CF[0x0212]="\u0213";
		CF[0x0214]="\u0215";
		CF[0x0216]="\u0217";
		CF[0x0218]="\u0219";
		CF[0x021A]="\u021B";
		CF[0x021C]="\u021D";
		CF[0x021E]="\u021F";
		CF[0x0222]="\u0223";
		CF[0x0224]="\u0225";
		CF[0x0226]="\u0227";
		CF[0x0228]="\u0229";
		CF[0x022A]="\u022B";
		CF[0x022C]="\u022D";
		CF[0x022E]="\u022F";
		CF[0x0230]="\u0231";
		CF[0x0232]="\u0233";
		CF[0x0345]="\u03B9";
		CF[0x0386]="\u03AC";
		CF[0x0388]="\u03AD";
		CF[0x0389]="\u03AE";
		CF[0x038A]="\u03AF";
		CF[0x038C]="\u03CC";
		CF[0x038E]="\u03CD";
		CF[0x038F]="\u03CE";
		CF[0x0390]="\u03B9\u0308\u0301";
		CF[0x0391]="\u03B1";
		CF[0x0392]="\u03B2";
		CF[0x0393]="\u03B3";
		CF[0x0394]="\u03B4";
		CF[0x0395]="\u03B5";
		CF[0x0396]="\u03B6";
		CF[0x0397]="\u03B7";
		CF[0x0398]="\u03B8";
		CF[0x0399]="\u03B9";
		CF[0x039A]="\u03BA";
		CF[0x039B]="\u03BB";
		CF[0x039C]="\u03BC";
		CF[0x039D]="\u03BD";
		CF[0x039E]="\u03BE";
		CF[0x039F]="\u03BF";
		CF[0x03A0]="\u03C0";
		CF[0x03A1]="\u03C1";
		CF[0x03A3]="\u03C2";
		CF[0x03A4]="\u03C4";
		CF[0x03A5]="\u03C5";
		CF[0x03A6]="\u03C6";
		CF[0x03A7]="\u03C7";
		CF[0x03A8]="\u03C8";
		CF[0x03A9]="\u03C9";
		CF[0x03AA]="\u03CA";
		CF[0x03AB]="\u03CB";
		CF[0x03B0]="\u03C5\u0308\u0301";
		CF[0x03C3]="\u03C2";
		CF[0x03D0]="\u03B2";
		CF[0x03D1]="\u03B8";
		CF[0x03D5]="\u03C6";
		CF[0x03D6]="\u03C0";
		CF[0x03DA]="\u03DB";
		CF[0x03DC]="\u03DD";
		CF[0x03DE]="\u03DF";
		CF[0x03E0]="\u03E1";
		CF[0x03E2]="\u03E3";
		CF[0x03E4]="\u03E5";
		CF[0x03E6]="\u03E7";
		CF[0x03E8]="\u03E9";
		CF[0x03EA]="\u03EB";
		CF[0x03EC]="\u03ED";
		CF[0x03EE]="\u03EF";
		CF[0x03F0]="\u03BA";
		CF[0x03F1]="\u03C1";
		CF[0x03F2]="\u03C2";
		CF[0x0400]="\u0450";
		CF[0x0401]="\u0451";
		CF[0x0402]="\u0452";
		CF[0x0403]="\u0453";
		CF[0x0404]="\u0454";
		CF[0x0405]="\u0455";
		CF[0x0406]="\u0456";
		CF[0x0407]="\u0457";
		CF[0x0408]="\u0458";
		CF[0x0409]="\u0459";
		CF[0x040A]="\u045A";
		CF[0x040B]="\u045B";
		CF[0x040C]="\u045C";
		CF[0x040D]="\u045D";
		CF[0x040E]="\u045E";
		CF[0x040F]="\u045F";
		CF[0x0410]="\u0430";
		CF[0x0411]="\u0431";
		CF[0x0412]="\u0432";
		CF[0x0413]="\u0433";
		CF[0x0414]="\u0434";
		CF[0x0415]="\u0435";
		CF[0x0416]="\u0436";
		CF[0x0417]="\u0437";
		CF[0x0418]="\u0438";
		CF[0x0419]="\u0439";
		CF[0x041A]="\u043A";
		CF[0x041B]="\u043B";
		CF[0x041C]="\u043C";
		CF[0x041D]="\u043D";
		CF[0x041E]="\u043E";
		CF[0x041F]="\u043F";
		CF[0x0420]="\u0440";
		CF[0x0421]="\u0441";
		CF[0x0422]="\u0442";
		CF[0x0423]="\u0443";
		CF[0x0424]="\u0444";
		CF[0x0425]="\u0445";
		CF[0x0426]="\u0446";
		CF[0x0427]="\u0447";
		CF[0x0428]="\u0448";
		CF[0x0429]="\u0449";
		CF[0x042A]="\u044A";
		CF[0x042B]="\u044B";
		CF[0x042C]="\u044C";
		CF[0x042D]="\u044D";
		CF[0x042E]="\u044E";
		CF[0x042F]="\u044F";
		CF[0x0460]="\u0461";
		CF[0x0462]="\u0463";
		CF[0x0464]="\u0465";
		CF[0x0466]="\u0467";
		CF[0x0468]="\u0469";
		CF[0x046A]="\u046B";
		CF[0x046C]="\u046D";
		CF[0x046E]="\u046F";
		CF[0x0470]="\u0471";
		CF[0x0472]="\u0473";
		CF[0x0474]="\u0475";
		CF[0x0476]="\u0477";
		CF[0x0478]="\u0479";
		CF[0x047A]="\u047B";
		CF[0x047C]="\u047D";
		CF[0x047E]="\u047F";
		CF[0x0480]="\u0481";
		CF[0x048C]="\u048D";
		CF[0x048E]="\u048F";
		CF[0x0490]="\u0491";
		CF[0x0492]="\u0493";
		CF[0x0494]="\u0495";
		CF[0x0496]="\u0497";
		CF[0x0498]="\u0499";
		CF[0x049A]="\u049B";
		CF[0x049C]="\u049D";
		CF[0x049E]="\u049F";
		CF[0x04A0]="\u04A1";
		CF[0x04A2]="\u04A3";
		CF[0x04A4]="\u04A5";
		CF[0x04A6]="\u04A7";
		CF[0x04A8]="\u04A9";
		CF[0x04AA]="\u04AB";
		CF[0x04AC]="\u04AD";
		CF[0x04AE]="\u04AF";
		CF[0x04B0]="\u04B1";
		CF[0x04B2]="\u04B3";
		CF[0x04B4]="\u04B5";
		CF[0x04B6]="\u04B7";
		CF[0x04B8]="\u04B9";
		CF[0x04BA]="\u04BB";
		CF[0x04BC]="\u04BD";
		CF[0x04BE]="\u04BF";
		CF[0x04C1]="\u04C2";
		CF[0x04C3]="\u04C4";
		CF[0x04C7]="\u04C8";
		CF[0x04CB]="\u04CC";
		CF[0x04D0]="\u04D1";
		CF[0x04D2]="\u04D3";
		CF[0x04D4]="\u04D5";
		CF[0x04D6]="\u04D7";
		CF[0x04D8]="\u04D9";
		CF[0x04DA]="\u04DB";
		CF[0x04DC]="\u04DD";
		CF[0x04DE]="\u04DF";
		CF[0x04E0]="\u04E1";
		CF[0x04E2]="\u04E3";
		CF[0x04E4]="\u04E5";
		CF[0x04E6]="\u04E7";
		CF[0x04E8]="\u04E9";
		CF[0x04EA]="\u04EB";
		CF[0x04EC]="\u04ED";
		CF[0x04EE]="\u04EF";
		CF[0x04F0]="\u04F1";
		CF[0x04F2]="\u04F3";
		CF[0x04F4]="\u04F5";
		CF[0x04F8]="\u04F9";
		CF[0x0531]="\u0561";
		CF[0x0532]="\u0562";
		CF[0x0533]="\u0563";
		CF[0x0534]="\u0564";
		CF[0x0535]="\u0565";
		CF[0x0536]="\u0566";
		CF[0x0537]="\u0567";
		CF[0x0538]="\u0568";
		CF[0x0539]="\u0569";
		CF[0x053A]="\u056A";
		CF[0x053B]="\u056B";
		CF[0x053C]="\u056C";
		CF[0x053D]="\u056D";
		CF[0x053E]="\u056E";
		CF[0x053F]="\u056F";
		CF[0x0540]="\u0570";
		CF[0x0541]="\u0571";
		CF[0x0542]="\u0572";
		CF[0x0543]="\u0573";
		CF[0x0544]="\u0574";
		CF[0x0545]="\u0575";
		CF[0x0546]="\u0576";
		CF[0x0547]="\u0577";
		CF[0x0548]="\u0578";
		CF[0x0549]="\u0579";
		CF[0x054A]="\u057A";
		CF[0x054B]="\u057B";
		CF[0x054C]="\u057C";
		CF[0x054D]="\u057D";
		CF[0x054E]="\u057E";
		CF[0x054F]="\u057F";
		CF[0x0550]="\u0580";
		CF[0x0551]="\u0581";
		CF[0x0552]="\u0582";
		CF[0x0553]="\u0583";
		CF[0x0554]="\u0584";
		CF[0x0555]="\u0585";
		CF[0x0556]="\u0586";
		CF[0x0587]="\u0565\u0582";
		CF[0x1E00]="\u1E01";
		CF[0x1E02]="\u1E03";
		CF[0x1E04]="\u1E05";
		CF[0x1E06]="\u1E07";
		CF[0x1E08]="\u1E09";
		CF[0x1E0A]="\u1E0B";
		CF[0x1E0C]="\u1E0D";
		CF[0x1E0E]="\u1E0F";
		CF[0x1E10]="\u1E11";
		CF[0x1E12]="\u1E13";
		CF[0x1E14]="\u1E15";
		CF[0x1E16]="\u1E17";
		CF[0x1E18]="\u1E19";
		CF[0x1E1A]="\u1E1B";
		CF[0x1E1C]="\u1E1D";
		CF[0x1E1E]="\u1E1F";
		CF[0x1E20]="\u1E21";
		CF[0x1E22]="\u1E23";
		CF[0x1E24]="\u1E25";
		CF[0x1E26]="\u1E27";
		CF[0x1E28]="\u1E29";
		CF[0x1E2A]="\u1E2B";
		CF[0x1E2C]="\u1E2D";
		CF[0x1E2E]="\u1E2F";
		CF[0x1E30]="\u1E31";
		CF[0x1E32]="\u1E33";
		CF[0x1E34]="\u1E35";
		CF[0x1E36]="\u1E37";
		CF[0x1E38]="\u1E39";
		CF[0x1E3A]="\u1E3B";
		CF[0x1E3C]="\u1E3D";
		CF[0x1E3E]="\u1E3F";
		CF[0x1E40]="\u1E41";
		CF[0x1E42]="\u1E43";
		CF[0x1E44]="\u1E45";
		CF[0x1E46]="\u1E47";
		CF[0x1E48]="\u1E49";
		CF[0x1E4A]="\u1E4B";
		CF[0x1E4C]="\u1E4D";
		CF[0x1E4E]="\u1E4F";
		CF[0x1E50]="\u1E51";
		CF[0x1E52]="\u1E53";
		CF[0x1E54]="\u1E55";
		CF[0x1E56]="\u1E57";
		CF[0x1E58]="\u1E59";
		CF[0x1E5A]="\u1E5B";
		CF[0x1E5C]="\u1E5D";
		CF[0x1E5E]="\u1E5F";
		CF[0x1E60]="\u1E61";
		CF[0x1E62]="\u1E63";
		CF[0x1E64]="\u1E65";
		CF[0x1E66]="\u1E67";
		CF[0x1E68]="\u1E69";
		CF[0x1E6A]="\u1E6B";
		CF[0x1E6C]="\u1E6D";
		CF[0x1E6E]="\u1E6F";
		CF[0x1E70]="\u1E71";
		CF[0x1E72]="\u1E73";
		CF[0x1E74]="\u1E75";
		CF[0x1E76]="\u1E77";
		CF[0x1E78]="\u1E79";
		CF[0x1E7A]="\u1E7B";
		CF[0x1E7C]="\u1E7D";
		CF[0x1E7E]="\u1E7F";
		CF[0x1E80]="\u1E81";
		CF[0x1E82]="\u1E83";
		CF[0x1E84]="\u1E85";
		CF[0x1E86]="\u1E87";
		CF[0x1E88]="\u1E89";
		CF[0x1E8A]="\u1E8B";
		CF[0x1E8C]="\u1E8D";
		CF[0x1E8E]="\u1E8F";
		CF[0x1E90]="\u1E91";
		CF[0x1E92]="\u1E93";
		CF[0x1E94]="\u1E95";
		CF[0x1E96]="\u0068\u0331";
		CF[0x1E97]="\u0074\u0308";
		CF[0x1E98]="\u0077\u030A";
		CF[0x1E99]="\u0079\u030A";
		CF[0x1E9A]="\u0061\u02BE";
		CF[0x1E9B]="\u1E61";
		CF[0x1EA0]="\u1EA1";
		CF[0x1EA2]="\u1EA3";
		CF[0x1EA4]="\u1EA5";
		CF[0x1EA6]="\u1EA7";
		CF[0x1EA8]="\u1EA9";
		CF[0x1EAA]="\u1EAB";
		CF[0x1EAC]="\u1EAD";
		CF[0x1EAE]="\u1EAF";
		CF[0x1EB0]="\u1EB1";
		CF[0x1EB2]="\u1EB3";
		CF[0x1EB4]="\u1EB5";
		CF[0x1EB6]="\u1EB7";
		CF[0x1EB8]="\u1EB9";
		CF[0x1EBA]="\u1EBB";
		CF[0x1EBC]="\u1EBD";
		CF[0x1EBE]="\u1EBF";
		CF[0x1EC0]="\u1EC1";
		CF[0x1EC2]="\u1EC3";
		CF[0x1EC4]="\u1EC5";
		CF[0x1EC6]="\u1EC7";
		CF[0x1EC8]="\u1EC9";
		CF[0x1ECA]="\u1ECB";
		CF[0x1ECC]="\u1ECD";
		CF[0x1ECE]="\u1ECF";
		CF[0x1ED0]="\u1ED1";
		CF[0x1ED2]="\u1ED3";
		CF[0x1ED4]="\u1ED5";
		CF[0x1ED6]="\u1ED7";
		CF[0x1ED8]="\u1ED9";
		CF[0x1EDA]="\u1EDB";
		CF[0x1EDC]="\u1EDD";
		CF[0x1EDE]="\u1EDF";
		CF[0x1EE0]="\u1EE1";
		CF[0x1EE2]="\u1EE3";
		CF[0x1EE4]="\u1EE5";
		CF[0x1EE6]="\u1EE7";
		CF[0x1EE8]="\u1EE9";
		CF[0x1EEA]="\u1EEB";
		CF[0x1EEC]="\u1EED";
		CF[0x1EEE]="\u1EEF";
		CF[0x1EF0]="\u1EF1";
		CF[0x1EF2]="\u1EF3";
		CF[0x1EF4]="\u1EF5";
		CF[0x1EF6]="\u1EF7";
		CF[0x1EF8]="\u1EF9";
		CF[0x1F08]="\u1F00";
		CF[0x1F09]="\u1F01";
		CF[0x1F0A]="\u1F02";
		CF[0x1F0B]="\u1F03";
		CF[0x1F0C]="\u1F04";
		CF[0x1F0D]="\u1F05";
		CF[0x1F0E]="\u1F06";
		CF[0x1F0F]="\u1F07";
		CF[0x1F18]="\u1F10";
		CF[0x1F19]="\u1F11";
		CF[0x1F1A]="\u1F12";
		CF[0x1F1B]="\u1F13";
		CF[0x1F1C]="\u1F14";
		CF[0x1F1D]="\u1F15";
		CF[0x1F28]="\u1F20";
		CF[0x1F29]="\u1F21";
		CF[0x1F2A]="\u1F22";
		CF[0x1F2B]="\u1F23";
		CF[0x1F2C]="\u1F24";
		CF[0x1F2D]="\u1F25";
		CF[0x1F2E]="\u1F26";
		CF[0x1F2F]="\u1F27";
		CF[0x1F38]="\u1F30";
		CF[0x1F39]="\u1F31";
		CF[0x1F3A]="\u1F32";
		CF[0x1F3B]="\u1F33";
		CF[0x1F3C]="\u1F34";
		CF[0x1F3D]="\u1F35";
		CF[0x1F3E]="\u1F36";
		CF[0x1F3F]="\u1F37";
		CF[0x1F48]="\u1F40";
		CF[0x1F49]="\u1F41";
		CF[0x1F4A]="\u1F42";
		CF[0x1F4B]="\u1F43";
		CF[0x1F4C]="\u1F44";
		CF[0x1F4D]="\u1F45";
		CF[0x1F50]="\u03C5\u0313";
		CF[0x1F52]="\u03C5\u0313\u0300";
		CF[0x1F54]="\u03C5\u0313\u0301";
		CF[0x1F56]="\u03C5\u0313\u0342";
		CF[0x1F59]="\u1F51";
		CF[0x1F5B]="\u1F53";
		CF[0x1F5D]="\u1F55";
		CF[0x1F5F]="\u1F57";
		CF[0x1F68]="\u1F60";
		CF[0x1F69]="\u1F61";
		CF[0x1F6A]="\u1F62";
		CF[0x1F6B]="\u1F63";
		CF[0x1F6C]="\u1F64";
		CF[0x1F6D]="\u1F65";
		CF[0x1F6E]="\u1F66";
		CF[0x1F6F]="\u1F67";
		CF[0x1F80]="\u1F00\u03B9";
		CF[0x1F81]="\u1F01\u03B9";
		CF[0x1F82]="\u1F02\u03B9";
		CF[0x1F83]="\u1F03\u03B9";
		CF[0x1F84]="\u1F04\u03B9";
		CF[0x1F85]="\u1F05\u03B9";
		CF[0x1F86]="\u1F06\u03B9";
		CF[0x1F87]="\u1F07\u03B9";
		CF[0x1F88]="\u1F00\u03B9";
		CF[0x1F89]="\u1F01\u03B9";
		CF[0x1F8A]="\u1F02\u03B9";
		CF[0x1F8B]="\u1F03\u03B9";
		CF[0x1F8C]="\u1F04\u03B9";
		CF[0x1F8D]="\u1F05\u03B9";
		CF[0x1F8E]="\u1F06\u03B9";
		CF[0x1F8F]="\u1F07\u03B9";
		CF[0x1F90]="\u1F20\u03B9";
		CF[0x1F91]="\u1F21\u03B9";
		CF[0x1F92]="\u1F22\u03B9";
		CF[0x1F93]="\u1F23\u03B9";
		CF[0x1F94]="\u1F24\u03B9";
		CF[0x1F95]="\u1F25\u03B9";
		CF[0x1F96]="\u1F26\u03B9";
		CF[0x1F97]="\u1F27\u03B9";
		CF[0x1F98]="\u1F20\u03B9";
		CF[0x1F99]="\u1F21\u03B9";
		CF[0x1F9A]="\u1F22\u03B9";
		CF[0x1F9B]="\u1F23\u03B9";
		CF[0x1F9C]="\u1F24\u03B9";
		CF[0x1F9D]="\u1F25\u03B9";
		CF[0x1F9E]="\u1F26\u03B9";
		CF[0x1F9F]="\u1F27\u03B9";
		CF[0x1FA0]="\u1F60\u03B9";
		CF[0x1FA1]="\u1F61\u03B9";
		CF[0x1FA2]="\u1F62\u03B9";
		CF[0x1FA3]="\u1F63\u03B9";
		CF[0x1FA4]="\u1F64\u03B9";
		CF[0x1FA5]="\u1F65\u03B9";
		CF[0x1FA6]="\u1F66\u03B9";
		CF[0x1FA7]="\u1F67\u03B9";
		CF[0x1FA8]="\u1F60\u03B9";
		CF[0x1FA9]="\u1F61\u03B9";
		CF[0x1FAA]="\u1F62\u03B9";
		CF[0x1FAB]="\u1F63\u03B9";
		CF[0x1FAC]="\u1F64\u03B9";
		CF[0x1FAD]="\u1F65\u03B9";
		CF[0x1FAE]="\u1F66\u03B9";
		CF[0x1FAF]="\u1F67\u03B9";
		CF[0x1FB2]="\u1F70\u03B9";
		CF[0x1FB3]="\u03B1\u03B9";
		CF[0x1FB4]="\u03AC\u03B9";
		CF[0x1FB6]="\u03B1\u0342";
		CF[0x1FB7]="\u03B1\u0342\u03B9";
		CF[0x1FB8]="\u1FB0";
		CF[0x1FB9]="\u1FB1";
		CF[0x1FBA]="\u1F70";
		CF[0x1FBB]="\u1F71";
		CF[0x1FBC]="\u03B1\u03B9";
		CF[0x1FBE]="\u03B9";
		CF[0x1FC2]="\u1F74\u03B9";
		CF[0x1FC3]="\u03B7\u03B9";
		CF[0x1FC4]="\u03AE\u03B9";
		CF[0x1FC6]="\u03B7\u0342";
		CF[0x1FC7]="\u03B7\u0342\u03B9";
		CF[0x1FC8]="\u1F72";
		CF[0x1FC9]="\u1F73";
		CF[0x1FCA]="\u1F74";
		CF[0x1FCB]="\u1F75";
		CF[0x1FCC]="\u03B7\u03B9";
		CF[0x1FD2]="\u03B9\u0308\u0300";
		CF[0x1FD3]="\u03B9\u0308\u0301";
		CF[0x1FD6]="\u03B9\u0342";
		CF[0x1FD7]="\u03B9\u0308\u0342";
		CF[0x1FD8]="\u1FD0";
		CF[0x1FD9]="\u1FD1";
		CF[0x1FDA]="\u1F76";
		CF[0x1FDB]="\u1F77";
		CF[0x1FE2]="\u03C5\u0308\u0300";
		CF[0x1FE3]="\u03C5\u0308\u0301";
		CF[0x1FE4]="\u03C1\u0313";
		CF[0x1FE6]="\u03C5\u0342";
		CF[0x1FE7]="\u03C5\u0308\u0342";
		CF[0x1FE8]="\u1FE0";
		CF[0x1FE9]="\u1FE1";
		CF[0x1FEA]="\u1F7A";
		CF[0x1FEB]="\u1F7B";
		CF[0x1FEC]="\u1FE5";
		CF[0x1FF2]="\u1F7C\u03B9";
		CF[0x1FF3]="\u03C9\u03B9";
		CF[0x1FF4]="\u03CE\u03B9";
		CF[0x1FF6]="\u03C9\u0342";
		CF[0x1FF7]="\u03C9\u0342\u03B9";
		CF[0x1FF8]="\u1F78";
		CF[0x1FF9]="\u1F79";
		CF[0x1FFA]="\u1F7C";
		CF[0x1FFB]="\u1F7D";
		CF[0x1FFC]="\u03C9\u03B9";
		CF[0x2126]="\u03C9";
		CF[0x212A]="\u006B";
		CF[0x212B]="\u00E5";
		CF[0x2160]="\u2170";
		CF[0x2161]="\u2171";
		CF[0x2162]="\u2172";
		CF[0x2163]="\u2173";
		CF[0x2164]="\u2174";
		CF[0x2165]="\u2175";
		CF[0x2166]="\u2176";
		CF[0x2167]="\u2177";
		CF[0x2168]="\u2178";
		CF[0x2169]="\u2179";
		CF[0x216A]="\u217A";
		CF[0x216B]="\u217B";
		CF[0x216C]="\u217C";
		CF[0x216D]="\u217D";
		CF[0x216E]="\u217E";
		CF[0x216F]="\u217F";
		CF[0x24B6]="\u24D0";
		CF[0x24B7]="\u24D1";
		CF[0x24B8]="\u24D2";
		CF[0x24B9]="\u24D3";
		CF[0x24BA]="\u24D4";
		CF[0x24BB]="\u24D5";
		CF[0x24BC]="\u24D6";
		CF[0x24BD]="\u24D7";
		CF[0x24BE]="\u24D8";
		CF[0x24BF]="\u24D9";
		CF[0x24C0]="\u24DA";
		CF[0x24C1]="\u24DB";
		CF[0x24C2]="\u24DC";
		CF[0x24C3]="\u24DD";
		CF[0x24C4]="\u24DE";
		CF[0x24C5]="\u24DF";
		CF[0x24C6]="\u24E0";
		CF[0x24C7]="\u24E1";
		CF[0x24C8]="\u24E2";
		CF[0x24C9]="\u24E3";
		CF[0x24CA]="\u24E4";
		CF[0x24CB]="\u24E5";
		CF[0x24CC]="\u24E6";
		CF[0x24CD]="\u24E7";
		CF[0x24CE]="\u24E8";
		CF[0x24CF]="\u24E9";
		CF[0xFB00]="\u0066\u0066";
		CF[0xFB01]="\u0066\u0069";
		CF[0xFB02]="\u0066\u006C";
		CF[0xFB03]="\u0066\u0066\u0069";
		CF[0xFB04]="\u0066\u0066\u006C";
		CF[0xFB05]="\u0073\u0074";
		CF[0xFB06]="\u0073\u0074";
		CF[0xFB13]="\u0574\u0576";
		CF[0xFB14]="\u0574\u0565";
		CF[0xFB15]="\u0574\u056B";
		CF[0xFB16]="\u057E\u0576";
		CF[0xFB17]="\u0574\u056D";
		CF[0xFF21]="\uFF41";
		CF[0xFF22]="\uFF42";
		CF[0xFF23]="\uFF43";
		CF[0xFF24]="\uFF44";
		CF[0xFF25]="\uFF45";
		CF[0xFF26]="\uFF46";
		CF[0xFF27]="\uFF47";
		CF[0xFF28]="\uFF48";
		CF[0xFF29]="\uFF49";
		CF[0xFF2A]="\uFF4A";
		CF[0xFF2B]="\uFF4B";
		CF[0xFF2C]="\uFF4C";
		CF[0xFF2D]="\uFF4D";
		CF[0xFF2E]="\uFF4E";
		CF[0xFF2F]="\uFF4F";
		CF[0xFF30]="\uFF50";
		CF[0xFF31]="\uFF51";
		CF[0xFF32]="\uFF52";
		CF[0xFF33]="\uFF53";
		CF[0xFF34]="\uFF54";
		CF[0xFF35]="\uFF55";
		CF[0xFF36]="\uFF56";
		CF[0xFF37]="\uFF57";
		CF[0xFF38]="\uFF58";
		CF[0xFF39]="\uFF59";
		CF[0xFF3A]="\uFF5A";
		// 785 case foldings total
	}
}