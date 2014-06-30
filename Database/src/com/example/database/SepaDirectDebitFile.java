package com.example.database;

import java.io.File;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;

import android.annotation.SuppressLint;
import android.content.Context;

import android.os.Environment;
import android.util.Log;

import org.w3c.dom.*;

public class SepaDirectDebitFile {

	public static final String INITIAL_STRING = "<?xml version='1.0' encoding='utf-8'?><Document xmlns='urn:iso:std:iso:20022:tech:xsd:pain.008.001.02' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'></Document>";
	private Document xml;
	private DatabaseHelper myDbHelper = null;

	@SuppressLint("NewApi")
	public SepaDirectDebitFile(Integer numberOfTransactions, Context context,
			Integer run) {
		myDbHelper = new DatabaseHelper(context);
		try {
			xml = loadXMLFromString(INITIAL_STRING);
		} catch (Exception e) {
			e.printStackTrace();
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmss",
				Locale.GERMANY);
		SimpleDateFormat dateCFormat = new SimpleDateFormat(
				"yyyy-MM-dd\tHH:mm:ss", Locale.GERMANY);
		SimpleDateFormat dateRFormat = new SimpleDateFormat("yyyy-MM-dd",
				Locale.GERMANY);
		SimpleDateFormat dateKFormat = new SimpleDateFormat("yyyy_MM",
				Locale.GERMANY);
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, 3);

		calendar.getTime();
		Map<String, String> prefs = myDbHelper.getPreferncies();

		Element documentElement = xml.getDocumentElement();
		Element CstmrDrctDbtInitn = xml.createElement("CstmrDrctDbtInitn");
		Element GrpHdr = xml.createElement("GrpHdr");
		Element MsgId = xml.createElement("MsgId");
		MsgId.setTextContent(dateFormat.format(new Date()));
		Element CreDtTm = xml.createElement("CreDtTm");
		CreDtTm.setTextContent(dateCFormat.format(new Date()));
		Element NbOfTxs = xml.createElement("NbOfTxs");
		NbOfTxs.setTextContent(String.valueOf(numberOfTransactions));
		Element InitgPty = xml.createElement("InitgPty");
		Element Nm = xml.createElement("Nm");
		Nm.setTextContent(prefs.get("name"));
		InitgPty.appendChild(Nm);
		GrpHdr.appendChild(MsgId);
		GrpHdr.appendChild(CreDtTm);
		GrpHdr.appendChild(NbOfTxs);
		GrpHdr.appendChild(InitgPty);

		Element PmtInf = xml.createElement("PmtInf");
		Element PmtInfId = xml.createElement("PmtInfId");
		PmtInfId.setTextContent(dateKFormat.format(new Date()) + "_"
				+ String.valueOf(run));
		Element PmtMtd = xml.createElement("PmtMtd");
		PmtMtd.setTextContent("DD");
		Element PmtTpInf = xml.createElement("PmtTpInf");
		Element SvcLvl = xml.createElement("SvcLvl");
		Element Cd = xml.createElement("Cd");
		Cd.setTextContent("SEPA");
		SvcLvl.appendChild(Cd);
		PmtTpInf.appendChild(SvcLvl);

		Element LclInstrm = xml.createElement("LclInstrm");
		Element Cd2 = xml.createElement("Cd");
		Cd2.setTextContent("CORE");
		LclInstrm.appendChild(Cd2);
		PmtTpInf.appendChild(LclInstrm);

		Element SeqTp = xml.createElement("SeqTp");
		SeqTp.setTextContent("OOFF");
		PmtTpInf.appendChild(SeqTp);

		Element ReqdColltnDt = xml.createElement("ReqdColltnDt");
		ReqdColltnDt.setTextContent(dateRFormat.format(calendar.getTime()));

		Element Cdtr = xml.createElement("Cdtr");
		Element Nm2 = xml.createElement("Nm");
		Nm2.setTextContent(prefs.get("name"));
		Cdtr.appendChild(Nm2);

		Element CdtrAcct = xml.createElement("CdtrAcct");
		Element Id = xml.createElement("Id");
		Element IBAN = xml.createElement("IBAN");
		IBAN.setTextContent(prefs.get("iban"));
		Id.appendChild(IBAN);
		CdtrAcct.appendChild(Id);

		Element CdtrAgt = xml.createElement("CdtrAgt");
		Element FinInstnId = xml.createElement("FinInstnId");
		Element BIC = xml.createElement("BIC");
		BIC.setTextContent(prefs.get("bic"));
		FinInstnId.appendChild(BIC);
		CdtrAgt.appendChild(FinInstnId);

		Element CdtrSchmeId = xml.createElement("CdtrSchmeId");
		Element Id3 = xml.createElement("Id");
		Element PrvtId = xml.createElement("PrvtId");
		Element Othr = xml.createElement("Othr");
		Element Id4 = xml.createElement("Id");
		Id4.setTextContent(prefs.get("glaeubigerid"));
		Othr.appendChild(Id4);
		Element SchmeNm = xml.createElement("SchmeNm");
		Element Prtry = xml.createElement("Prtry");
		Prtry.setTextContent("SEPA");
		SchmeNm.appendChild(Prtry);
		Othr.appendChild(SchmeNm);
		PrvtId.appendChild(Othr);
		Id3.appendChild(PrvtId);
		CdtrSchmeId.appendChild(Id3);

		PmtInf.appendChild(PmtInfId);
		PmtInf.appendChild(PmtMtd);
		PmtInf.appendChild(PmtTpInf);
		PmtInf.appendChild(ReqdColltnDt);
		PmtInf.appendChild(Cdtr);
		PmtInf.appendChild(CdtrAcct);
		PmtInf.appendChild(CdtrAgt);
		PmtInf.appendChild(CdtrSchmeId);

		PmtInf = myDbHelper.getTransactions(PmtInf, xml, run);

		CstmrDrctDbtInitn.appendChild(GrpHdr);
		CstmrDrctDbtInitn.appendChild(PmtInf);
		documentElement.appendChild(CstmrDrctDbtInitn);
		xml.replaceChild(documentElement, documentElement);

		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/dtaus/");
		dir.mkdir();

		File xmlFile = new File(dir, "SEPA_" + String.valueOf(run) + ".xml");
		Transformer tFormer;
		try {
			tFormer = TransformerFactory.newInstance().newTransformer();
			tFormer.setOutputProperty(OutputKeys.METHOD, "xml");

			Source source = new DOMSource(xml);
			StreamResult result = new StreamResult(xmlFile);
			try {
				tFormer.transform(source, result);
			} catch (TransformerException e) {
				e.printStackTrace();
			}
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}

	}

	public static Document loadXMLFromString(String xml) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xml));
		return builder.parse(is);
	}

}
