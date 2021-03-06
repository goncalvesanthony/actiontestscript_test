package com.ats.tools.report;

import com.ats.recorder.VisualAction;
import com.ats.recorder.VisualImage;
import com.ats.recorder.VisualReport;
import com.ats.recorder.ReportSummary;
import com.ats.tools.Utils;
import com.ats.tools.logger.ExecutionLogger;
import com.exadel.flamingo.flex.messaging.amf.io.AMF3Deserializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;

public class XmlReport {

	public static String REPORT_FILE = "actions.xml";

	public static void createReport(Path output, String qualifiedName, ExecutionLogger logger) {

		final File atsvFile = output.resolve(qualifiedName + ".atsv").toFile();

		if(atsvFile.exists()) {

			final File xmlFolder = output.resolve(qualifiedName + "_xml").toFile();
			logger.sendInfo("Create XML report", xmlFolder.getAbsolutePath());

			final ArrayList<VisualImage> imagesList = new ArrayList<VisualImage>();
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			try {
				Utils.deleteRecursive(xmlFolder);
			} catch (FileNotFoundException e) {}

			xmlFolder.mkdirs();
			final Path xmlFolderPath = xmlFolder.toPath();

			try {

				final DocumentBuilder builder = factory.newDocumentBuilder();
				final Document document = builder.newDocument();

				FileInputStream fis = null;
				AMF3Deserializer amf3 = null;

				try {

					fis = new FileInputStream(atsvFile);
					amf3 = new AMF3Deserializer(fis);

					final Element atsRoot = document.createElement("ats");
					document.appendChild(atsRoot);

					//------------------------------------------------------------------------------------------------------
					// script header
					//------------------------------------------------------------------------------------------------------

					final VisualReport report = (VisualReport) amf3.readObject();
					final Element script = document.createElement("script");

					atsRoot.appendChild(script);

					script.setAttribute("testId", report.getId());					
					script.setAttribute("testName", report.getName());

					script.setAttribute("cpuSpeed", String.valueOf(report.getCpuSpeed()));
					script.setAttribute("cpuCount", String.valueOf(report.getCpuCount()));
					script.setAttribute("totalMemory", String.valueOf(report.getTotalMemory()));		
					script.setAttribute("osInfo", report.getOsInfo());	

					Element description = document.createElement("description");
					description.setTextContent(report.getDescription());
					script.appendChild(description);

					Element author = document.createElement("author");
					author.setTextContent(report.getAuthor());
					script.appendChild(author);

					Element prerequisite = document.createElement("prerequisite");
					prerequisite.setTextContent(report.getPrerequisite());
					script.appendChild(prerequisite);

					Element started = document.createElement("started");
					started.setTextContent(report.getStarted());
					script.appendChild(started);

					Element groups = document.createElement("groups");
					groups.setTextContent(report.getGroups());
					script.appendChild(groups);

					Element quality = document.createElement("quality");
					quality.setTextContent(String.valueOf(report.getQuality()));
					script.appendChild(quality);

					//------------------------------------------------------------------------------------------------------
					//------------------------------------------------------------------------------------------------------

					Element actions = document.createElement("actions");
					atsRoot.appendChild(actions);

					while(amf3.available() > 0) {

						final Object obj = amf3.readObject();

						if(obj instanceof VisualAction) {

							final VisualAction va = (VisualAction) obj;

							final Element action = document.createElement("action");
							action.setAttribute("index", String.valueOf(va.getIndex()));
							action.setAttribute("type", va.getType());

							action.appendChild(document.createElement("line")).setTextContent(String.valueOf(va.getLine()));
							action.appendChild(document.createElement("script")).setTextContent(va.getScript());
							action.appendChild(document.createElement("timeLine")).setTextContent(String.valueOf(va.getTimeLine()));
							action.appendChild(document.createElement("error")).setTextContent(String.valueOf(va.getError()));
							action.appendChild(document.createElement("stop")).setTextContent(String.valueOf(va.isStop()));
							action.appendChild(document.createElement("duration")).setTextContent(String.valueOf(va.getDuration()));
							action.appendChild(document.createElement("passed")).setTextContent((String.valueOf(va.getError() == 0)));
							action.appendChild(document.createElement("value")).setTextContent(va.getValue());
							action.appendChild(document.createElement("data")).setTextContent(va.getData());

							Element elem = document.createElement("img");
							elem.setAttribute("src", va.getImageFileName());
							elem.setAttribute("width", String.valueOf(va.getChannelBound().getWidth().intValue()));
							elem.setAttribute("height", String.valueOf(va.getChannelBound().getHeight().intValue()));
							action.appendChild(elem);

							Element channel = document.createElement("channel");
							channel.setAttribute("name", va.getChannelName());

							Element channelBound = document.createElement("bound");
							Element channelX = document.createElement("x");
							channelX.setTextContent(String.valueOf(va.getChannelBound().getX().intValue()));
							channelBound.appendChild(channelX);

							Element channelY = document.createElement("y");
							channelY.setTextContent(String.valueOf(va.getChannelBound().getY().intValue()));
							channelBound.appendChild(channelY);

							Element channelWidth = document.createElement("width");
							channelWidth.setTextContent(String.valueOf(va.getChannelBound().getWidth().intValue()));
							channelBound.appendChild(channelWidth);

							Element channelHeight = document.createElement("height");
							channelHeight.setTextContent(String.valueOf(va.getChannelBound().getHeight().intValue()));
							channelBound.appendChild(channelHeight);

							channel.appendChild(channelBound);
							action.appendChild(channel);

							if(va.getElement() != null) {

								Element element = document.createElement("element");
								element.setAttribute("tag", va.getElement().getTag());

								Element criterias = document.createElement("criterias");
								criterias.setTextContent(va.getElement().getCriterias());
								element.appendChild(criterias);

								Element foundElements = document.createElement("foundElements");
								foundElements.setTextContent(String.valueOf(va.getElement().getFoundElements()));
								element.appendChild(foundElements);

								Element searchDuration = document.createElement("searchDuration");
								searchDuration.setTextContent(String.valueOf(va.getElement().getSearchDuration()));
								element.appendChild(searchDuration);

								Element elementBound = document.createElement("bound");
								Element elementX = document.createElement("x");
								elementX.setTextContent(String.valueOf(va.getElement().getBound().getX().intValue()));
								elementBound.appendChild(elementX);

								Element elementY = document.createElement("y");
								elementY.setTextContent(String.valueOf(va.getElement().getBound().getY().intValue()));
								elementBound.appendChild(elementY);

								Element elementWidth = document.createElement("width");
								elementWidth.setTextContent(String.valueOf(va.getElement().getBound().getWidth().intValue()));
								elementBound.appendChild(elementWidth);

								Element elementHeight = document.createElement("height");
								elementHeight.setTextContent(String.valueOf(va.getElement().getBound().getHeight().intValue()));
								elementBound.appendChild(elementHeight);

								element.appendChild(elementBound);
								action.appendChild(element);
							}

							actions.appendChild(action);

							va.addImage(xmlFolderPath, imagesList);

						} else if(obj instanceof ReportSummary){
							
							final ReportSummary reportSummary = (ReportSummary)obj;
							
							final Element summary = document.createElement("summary");
							
							summary.setAttribute("actions", String.valueOf(reportSummary.getActions()));
							summary.setAttribute("suiteName", reportSummary.getSuiteName());
							summary.setAttribute("testName", reportSummary.getTestName());
							summary.setAttribute("status", String.valueOf(reportSummary.getStatus()));
							
							final Element data = document.createElement("data");
							if(!ReportSummary.EMPTY_VALUE.equals(reportSummary.getData())) {
								data.setTextContent(reportSummary.getData());
							}
							summary.appendChild(data);
							
							if(reportSummary.getStatus() == 0 && reportSummary.getError() != null) {
								final Element error = document.createElement("error");
								error.setAttribute("script", reportSummary.getError().getScriptName());
								error.setAttribute("line", String.valueOf(reportSummary.getError().getLine()));
								error.setTextContent(reportSummary.getError().getMessage());
								summary.appendChild(error);
							}							

							script.appendChild(summary);
						}
					}

				} catch (FileNotFoundException e0) {
					logger.sendError("XML report stream error ->", e0.getMessage());
				} catch (IOException e1) {
					logger.sendError("XML report file error ->", e1.getMessage());
				} catch (Exception e2) {
					logger.sendError("XML report exception ->", e2.getMessage());
				}finally {
					try {
						if(fis != null) {
							fis.close();
						}
					} catch (IOException e) {
						logger.sendError("XML report close stream error ->", e.getMessage());
					}
				}

				imagesList.parallelStream().forEach(im -> im.save());

				try {

					final BufferedWriter writer = Files.newBufferedWriter(xmlFolder.toPath().resolve(REPORT_FILE), StandardCharsets.UTF_8);
					final Transformer transformer = TransformerFactory.newInstance().newTransformer();
					transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
					
					transformer.transform(new DOMSource(document), new StreamResult(writer));

				} catch (TransformerConfigurationException e2) {
					logger.sendError("XML report config error ->", e2.getMessage());
				} catch (TransformerException e3) {
					logger.sendError("XML report transform error ->", e3.getMessage());
				} catch (FileNotFoundException e4) {
					logger.sendError("XML report write file error ->", e4.getMessage());
				} catch (IOException e5) {
					logger.sendError("XML report IO write file error ->", e5.getMessage());
				}

			} catch (ParserConfigurationException e4) {
				logger.sendError("XML report parser error ->", e4.getMessage());
			}

			logger.sendInfo("XML report generated", xmlFolder.getAbsolutePath());
		} else {
			///TODO Create empty xml
			final File xmlFolder = output.resolve(qualifiedName + "_xml").toFile();
			logger.sendInfo("Create empty XML report because no ATSV file founded", xmlFolder.getAbsolutePath());
			
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			
			try {
				Utils.deleteRecursive(xmlFolder);
			} catch (FileNotFoundException e) {}

			xmlFolder.mkdirs();
			
			try {

				final DocumentBuilder builder = factory.newDocumentBuilder();
				final Document document= builder.newDocument();

				try {

					final Element atsRoot = document.createElement("ats");
					document.appendChild(atsRoot);

					//------------------------------------------------------------------------------------------------------
					// script header
					//------------------------------------------------------------------------------------------------------
					final Element script = document.createElement("script");

					atsRoot.appendChild(script);

					script.setAttribute("testId", "");					
					script.setAttribute("testName", qualifiedName);

					script.setAttribute("cpuSpeed", "");
					script.setAttribute("cpuCount", "");
					script.setAttribute("totalMemory", "");		
					script.setAttribute("osInfo", "");	

					Element description = document.createElement("description");
					description.setTextContent("This script is empty");
					script.appendChild(description);

					Element author = document.createElement("author");
					author.setTextContent("");
					script.appendChild(author);

					Element prerequisite = document.createElement("prerequisite");
					prerequisite.setTextContent("");
					script.appendChild(prerequisite);

					Element started = document.createElement("started");
					started.setTextContent(String.valueOf(new Date().getTime()));
					script.appendChild(started);

					Element groups = document.createElement("groups");
					groups.setTextContent("");
					script.appendChild(groups);

					Element quality = document.createElement("quality");
					quality.setTextContent("");
					script.appendChild(quality);

					//------------------------------------------------------------------------------------------------------
					//------------------------------------------------------------------------------------------------------

					Element actions = document.createElement("actions");
					atsRoot.appendChild(actions);
				} catch (Exception e2) {
					logger.sendError("XML report exception", e2.getMessage());
				}finally {
				}

				try {

					final Transformer transformer = TransformerFactory.newInstance().newTransformer();
					transformer.transform(
							new DOMSource(document), 
							new StreamResult(
									new OutputStreamWriter(
											new FileOutputStream(
													xmlFolder.toPath().resolve(REPORT_FILE).toFile()), 
											StandardCharsets.UTF_8)));

				} catch (TransformerConfigurationException e2) {
					logger.sendError("XML report config error", e2.getMessage());
				} catch (TransformerException e3) {
					logger.sendError("XML report transform error", e3.getMessage());
				} catch (FileNotFoundException e4) {
					logger.sendError("XML report write file error", e4.getMessage());
				}

			} catch (ParserConfigurationException e4) {
				logger.sendError("XML report parser error", e4.getMessage());
			}

			logger.sendInfo("XML report generated", xmlFolder.getAbsolutePath());
			
		}
	}
}