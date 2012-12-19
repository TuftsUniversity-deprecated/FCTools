/*
 *
 * ModifyDataStream.java
 *
 * Created on  Aug 20, 2010
 *
 * Copyright 2003-2010 Tufts University  Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 */

import java.util.*;
import java.net.*;
import java.io.*;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;




import org.fcrepo.client.*;
import org.fcrepo.client.FedoraClient;
import org.fcrepo.client.utility.ingest.AutoIngestor;
import org.fcrepo.client.utility.AutoFinder;
import org.fcrepo.server.types.gen.Datastream;



/**
 * @author akumar03
 *
 */
public class ModifyDatastream {
	private static final Logger LOG =    Logger.getLogger(ModifyDatastream.class.getName());
	static ResourceBundle bundle = ResourceBundle.getBundle("fctools");
	public static final String FEDORA_ADDRESS =bundle.getString("fedora.address");
	public static final String FEDORA_SPORT = bundle.getString("fedora.sport");
	public static final String FEDORA_PORT = bundle.getString("fedora.port");
	public static final String FEDORA_USERNAME =  bundle.getString("fedora.username");
	public static final String FEDORA_PASSWORD = bundle.getString("fedora.password");
	public static final String FEDORA_SPROTOCOL = bundle.getString("fedora.sprotocol");
	public static final String FEDORA_TRUSTSTORE_PASSWORD= bundle.getString("fedora.truststore.password");
 	public static final String FEDORA_TRUSTSTORE = "truststore";
	
//	public static final String  TEST_ID = "test:oaiprovider-object-item-c.d_a.d";
//	public static final String  TEST_ID = "test:oaiprovider-object-item-c.d_a.n";
 	public static final String  TEST_ID = "test:oaiprovider-object-identify";
	public static final String FEDORA_URL_PATH = "/fedora/";
	public static final String XML_MIME_TYPE ="text/xml";
	public static final String RELS_DS = "RELS-EXT";
	public static final String RELS_LABEL ="Relationships to other objects";
	public static final String NS_RELS =bundle.getString("ns.rdf");
	public static final String NS_OAI = bundle.getString("ns.oai");
	public static final String COMMENT = "Added itemID for OAI harvesting";	    
	
	
	static DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	static DocumentBuilder builder; 
	static XPathFactory factory = XPathFactory.newInstance();
	static XPath xpath = factory.newXPath();
	static NamespaceContext context =    new OAINamespaceContext();
	static TransformerFactory transfac = TransformerFactory.newInstance();
	static Transformer trans;
	
	private FedoraClient fc;
	private String objectId = TEST_ID;
	
	private PrintStream out = System.out;
	public PrintStream getOut() {
		return out;
	}

	public void setOut(PrintStream out) {
		this.out = out;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		 ModifyDatastream md  = new ModifyDatastream();
		 md.addItemId();
	}
 	
	/**
	 *  
	 */
	public ModifyDatastream() throws Exception {
		  File dir = new File (".");

		String tPath =  dir.getCanonicalPath()+File.separator+ FEDORA_TRUSTSTORE;
		tPath = tPath.replaceAll("\\\\","\\\\\\\\" );
 		System.setProperty("javax.net.ssl.trustStore",tPath);
        System.setProperty("javax.net.ssl.trustStorePassword", FEDORA_TRUSTSTORE_PASSWORD);
    
		try {
		  domFactory.setNamespaceAware(true);
		  builder = domFactory.newDocumentBuilder();
		  System.out.println("**** BUILDER *****");
		  xpath.setNamespaceContext(context);
		  trans = transfac.newTransformer();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	/**
	 *  This method checks if biblographicCitation is there and purges it
	 *  
	 *  @throws Exception
	 */

	public void purgeBibliographicCitation(String pid) throws Exception {
		String datastream ;
		try {
			datastream  = getDCAMETADatatream(pid);
			if(datastream == null) {
				return;
			} else
			{
				removeCitationFieldFromDatastream(datastream,pid);
			}
		} catch(NullPointerException ex) {
			System.out.println("datastream is not present ");
			return;
		}		
		//String OAIItemId = getOAIItemID(datastream) ;
		//if(OAIItemId== null) {
		//	addOAIItemID(datastream);
		//} else {
		//	out.println(objectId +", "+ OAIItemId+" : OAI ItemID already present. skipping...");
		//}
	}

    private void removeCitationFieldFromDatastream(String datastream, String pid)
    {
    	try
    	{
    		//ByteArrayInputStream input = new ByteArrayInputStream(yourString.getBytes(perhapsEncoding));
    		String trimmed_string = datastream;
//String trimmed_string = "<dca_dc:dc xmlns:dca_dc=\"http://nils.lib.tufts.edu/dca_dc/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:dcatech=\"http://nils.lib.tufts.edu/dcatech/\" xmlns:dcadesc=\"http://nils.lib.tufts.edu/dcadesc/\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.fedora.info/definitions/\">" +
//				        "<dc:title>This I Believe</dc:title><dc:bibliographicCitation>Haynes, George Edmund. &quot;This I Believe.&quot; 1954-01-15. Tufts University. Digital Collections and Archives. Medford, MA. http://hdl.handle.net/10427/75869</dc:bibliographicCitation>" +				        
//			         	"</dca_dc:dc>";
	    	Document doc = builder.parse(new StringBufferInputStream(trimmed_string));
	    	System.out.println(doc.toString());
		    XPathExpression expr   = xpath.compile("//dc:bibliographicCitation");
		   // System.out.println("expression : " + expr.toString());
	        Node nodeToRemove = (Node) expr.evaluate(doc, XPathConstants.NODE);
	        
	        //System.out.println("node to remove" + nodeToRemove.getLength());
	        nodeToRemove.getParentNode().removeChild(nodeToRemove);	
		    StringWriter sw = new StringWriter();
	        StreamResult sr  = new StreamResult(sw);
	        DOMSource source = new DOMSource(doc);
	        trans.transform(source, sr);
	      //  TransformerFactory tf = TransformerFactory.newInstance();
	       // Transformer t = tf.newTransformer();
	     //   t.transform(source, sr);
	        String xmlString = sw.toString();
	      //  System.out.println("CLEANED");
	        System.out.println(xmlString);
	        fc = new FedoraClient(FEDORA_SPROTOCOL+"://"+FEDORA_ADDRESS+":"+FEDORA_SPORT+FEDORA_URL_PATH,FEDORA_USERNAME,FEDORA_PASSWORD);
    	    fc.getAPIM().modifyDatastreamByValue(pid,"DCA-META",null,"DCA Descriptive Metadata","text/xml",null,xmlString.getBytes(),null,null,"purging bib citation",true);
     	    fc = null;
      		System.gc();
        	out.println(pid+ ", Purged Bibliographic Citation");
	    }
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    	System.out.println("MODIFY FAILED FOR PID : " + pid);
	    }
    //    fc = new FedoraClient(FEDORA_SPROTOCOL+"://"+FEDORA_ADDRESS+":"+FEDORA_SPORT+FEDORA_URL_PATH,FEDORA_USERNAME,FEDORA_PASSWORD);
    //    fc.getAPIM().modifyDatastreamByValue(objectId,RELS_DS,null,RELS_LABEL,XML_MIME_TYPE,NS_RELS,xmlString.getBytes(),null,null,COMMENT,true);
     //   fc = null;
      //  System.gc();
        // out.println(objectId+ ", Added  OAI ItemID.");

	
    }
	/** 
	 *  Gets the RELS-EXT datastream of an object
	 *  
	 * @return
	 * @throws Exception
	 */
	public String  getDCAMETADatatream(String pid) throws Exception {
		String path  ="http://"+FEDORA_ADDRESS+":"+FEDORA_PORT+FEDORA_URL_PATH+"objects/"+pid+"/datastreams/DCA-META/content";
		LOG.debug("PATH"+path);
		URL url = new URL(path);
		StringBuffer datastream = new StringBuffer();
		BufferedInputStream bs;
		try  {
			bs = new BufferedInputStream(url.openStream());
				while(bs.available()>0){
					datastream.append((char)bs.read()); 
				}
				return datastream.toString();
		} catch(IOException ex) {
			out.println(objectId+"DCA-META Datastream or the object is not present");
			return null;
		}
               	
	}	
	/**
	 *  This method checks if itemId tag is present in RELS-EXT or adds it
	 *  
	 *  @throws Exception
	 */

	public void addItemId() throws Exception {
		String datastream ;
		try {
			datastream  = getRELSEXTDatatream();
			if(datastream == null) {
				return;
			}
//			System.out.println(datastream);
		} catch(NullPointerException ex) {
			System.out.println("datastream is not present ");
			return;
		}		
		String OAIItemId = getOAIItemID(datastream) ;
		if(OAIItemId== null) {
			addOAIItemID(datastream);
		} else {
			out.println(objectId +", "+ OAIItemId+" : OAI ItemID already present. skipping...");
		}
	}
	
	/**
	 * Adds itemID tag to RELS-EXT datastream 
	 * 
	 * @param datastream the XML datastream
	 * @throws Exception
	 */
	
	public void addOAIItemID(String datastream) throws Exception {
		Document doc = builder.parse(new StringBufferInputStream(datastream));
	    XPathExpression expr   = xpath.compile("//rdf:Description");
	    Object result = expr.evaluate(doc, XPathConstants.NODESET);
	    NodeList nodes = (NodeList) result;
	    Node rdfNode = nodes.item(0);
	    Element childElement = doc.createElementNS(NS_OAI,"itemID");
	    childElement.setTextContent("oai:"+objectId);
	    rdfNode.appendChild(childElement);
	    StringWriter sw = new StringWriter();
        StreamResult sr  = new StreamResult(sw);
        DOMSource source = new DOMSource(doc);
        trans.transform(source, sr);
        String xmlString = sw.toString();
//        System.out.println(xmlString);
        fc = new FedoraClient(FEDORA_SPROTOCOL+"://"+FEDORA_ADDRESS+":"+FEDORA_SPORT+FEDORA_URL_PATH,FEDORA_USERNAME,FEDORA_PASSWORD);
        fc.getAPIM().modifyDatastreamByValue(objectId,RELS_DS,null,RELS_LABEL,XML_MIME_TYPE,NS_RELS,xmlString.getBytes(),null,null,COMMENT,true);
        fc = null;
        System.gc();
         out.println(objectId+ ", Added  OAI ItemID.");

	}
	
	/** 
	 *  Gets the RELS-EXT datastream of an object
	 *  
	 * @return
	 * @throws Exception
	 */
	public String  getRELSEXTDatatream() throws Exception {
		String path  ="http://"+FEDORA_ADDRESS+":"+FEDORA_PORT+FEDORA_URL_PATH+"objects/"+objectId+"/datastreams/"+RELS_DS+"/content";
		LOG.debug("PATH"+path);
		URL url = new URL(path);
		StringBuffer datastream = new StringBuffer();
		BufferedInputStream bs;
		try  {
			bs = new BufferedInputStream(url.openStream());
				while(bs.available()>0){
					datastream.append((char)bs.read()); 
				}
				return datastream.toString();
		} catch(IOException ex) {
			out.println(objectId+" RELS-EXT Datastream or the object is not present");
			return null;
		}
               	
	}
	/*
	 * This method checks if the element itemID exists in the xml
	 * 
	 * @param xml the xml to be checked
	 * @return true if itemID exists
	 */
	
	@SuppressWarnings("deprecation")
	private String getOAIItemID(String xml) throws Exception {
	    Document doc = builder.parse(new StringBufferInputStream(xml));
	    XPathExpression expr 	     = xpath.compile("//oai:itemID/text()");
	    NodeList nodes  =(NodeList) expr.evaluate(doc, XPathConstants.NODESET);
	    // We return the first item id and ignore the rest;
	    if(nodes == null || nodes.getLength() == 0) {
	    	return null;
	    }
	    String itemID =nodes.item(0).getNodeValue();
	    // clean white spaces (of course assumes there are no white spaces in between id)
	    itemID = itemID.replaceAll("\\s+",""); 
//	    for (int i = 0; i < nodes.getLength(); i++) {
//	        System.out.println(nodes.item(i).getNodeValue()); 
//	    }
	    return itemID;
	}
	

}
class OAINamespaceContext implements NamespaceContext {	 
	static ResourceBundle bundle = ResourceBundle.getBundle("fctools");
	
	public static final String NS_OAI = bundle.getString("ns.oai");
	public static final String NS_RDF =  bundle.getString("ns.rdf");
	public static final String NS_FEDORA = bundle.getString("ns.fedora");
	public static final String DCA_DC= "http://nils.lib.tufts.edu/dca_dc/";
	public static final String DCA_DESC = "http://nils.lib.tufts.edu/dcadesc/";
	public static final String XLINK = "http://www.w3.org/TR/xlink";
	public static final String DCTERMS = "http://purl.org/dc/terms/";
	public static final String DCATECH = "http://nils.lib.tufts.edu/dcatech/";
	public static final String DC ="http://purl.org/dc/elements/1.1/";

	public String getNamespaceURI(String prefix) {
		String uri;
		System.out.println("NAME SPACE LOOKUP " + prefix);
		if (prefix.equals("oai"))
			uri = NS_OAI;
		else if (prefix.equals("rdf"))
			uri = NS_RDF;
		else if (prefix.equals("dca_dc"))
			uri = DCA_DC;
		else if (prefix.equals("dcadesc"))
			uri = DCA_DESC;
		else if (prefix.equals("xlink"))
			uri = XLINK;
		else if (prefix.equals("dcterms"))
			uri = DCTERMS;
		else if (prefix.equals("dcatech"))
			uri = DCATECH;
		else if (prefix.equals("dc"))
		{
			uri = DC;
			System.out.println("returning " + uri);
		}
		else
			uri = NS_FEDORA; //setting fedora UI as default
		return uri;
	}

	// Dummy implementation - not used!
	@SuppressWarnings("unchecked")
	public Iterator getPrefixes(String val) {
		return null;
	}
	//Dummy implementation - not used!
	public String getPrefix(String uri) {
		return null;
	}
}
