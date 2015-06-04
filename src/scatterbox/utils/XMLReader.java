package scatterbox.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLReader {

   String sensorType = "";
   String sensorID = ""; 
   String location = "";
   String device = "";
   int deviceCounter = 0;
   List<String> allObjects = new LinkedList<String>();
   List<String> allObjectLocations = new LinkedList<String>();
   

   String sensorString = "<owl:Thing rdf:about=\"#SENSORID\">"+
"<rdf:type rdf:resource=\"#SENSORTYPE\"/>"+
"<device:identifier rdf:datatype=\"&xsd;string\">SENSORID</device:identifier>"+
//"<location:locatedIn rdf:resource=\"#LOCATION\"/>" +
"<attachedTo rdf:resource=\"#OBJECT\"/>" +
"</owl:Thing>";    
   
   String resourceString = "<resource:PhysicalResource rdf:about=\"#OBJECT\">"+
        "<rdf:type rdf:resource=\"&owl;Thing\"/>"+
        "<location:locatedIn rdf:resource=\"#LOCATION\"/>"+
        "</resource:PhysicalResource>";
   String filename = "XMLtoRDF";
   File outputFile = new File(filename);
   FileWriter writer;
   
 public static void main(String argv[]) throws IOException {
    XMLReader x = new XMLReader();
    x.processPlacelabXMLFile();
    x.writer.close();
}

 private void processPlacelabXMLFile(){
    try {
       writer = new FileWriter(outputFile);
       
       File file = new File("/Users/seamusknox/Documents/datasets/placelab/PLObjects_oct23.xml");
       DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
       DocumentBuilder db;

         db = dbf.newDocumentBuilder();

       Document doc = db.parse(file);
       doc.getDocumentElement().normalize();
       System.out.println("Root element " + doc.getDocumentElement().getNodeName());
       NodeList nodeLst = doc.getElementsByTagName("SENSOR");
       for (int s = 0; s < nodeLst.getLength(); s++) {

         Node fstNode = nodeLst.item(s);
         
         if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
            NamedNodeMap nnp = fstNode.getAttributes();
            sensorType = nnp.getNamedItem("type").getTextContent();
            //System.out.println(sensorType);
            
           Element e = (Element)fstNode;
           //System.out.println(e.getAttribute("class"));
           
           Element fstElmnt = (Element) fstNode;
           
           NodeList idNode = fstElmnt.getElementsByTagName("ID");
           Element idElement = (Element) idNode.item(0);
           sensorID = idElement.getAttribute("id");
          // System.out.println("<"+sensorID+">");
           
           NodeList locationNode = fstElmnt.getElementsByTagName("LOCATION");
           Element locationElement = (Element) locationNode.item(0);
           location = locationElement.getAttribute("text");
          // System.out.println("<"+location+">");

           NodeList objectNode = fstElmnt.getElementsByTagName("OBJECT");
           Element objectElement = (Element) objectNode.item(0);
           device = objectElement.getAttribute("text");
           if(device.equalsIgnoreCase("")){
              device = "unknown_object";
           }
           
           device = device+deviceCounter;
           deviceCounter = deviceCounter + 1;
           if(!checkForDevice(device)){
              allObjects.add(device.replace(" ", ""));
              allObjectLocations.add(location.replace(" ", ""));
           }
         }
         String newSensor = sensorString;
         newSensor = newSensor.replace("SENSORID", sensorID.replace(" ", ""))
         .replace("SENSORTYPE", sensorType.replace(" ", ""))
         //.replace("LOCATION", location.replace(" ", ""))
         .replace("OBJECT", device.replace(" ", ""));
         System.out.println(newSensor);
         writer.append(newSensor);
         writer.append("\n");
       }
       outputDeviceDetails();
    } catch (ParserConfigurationException e1) {
       // TODO Auto-generated catch block
       e1.printStackTrace();
    } catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
   } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
   }
      }
 /**
  * Checks whether the device is contained in the list.
  * @param a_device
  * @return
  */
 private boolean checkForDevice(String a_device){
    for(String s:allObjects){
       if(a_device.equalsIgnoreCase(s)){
          return true;
       }
    }
    
    return false;
 }
 
 
 private void outputDeviceDetails() throws IOException{
    for(int i=0;i<allObjects.size();i++){
       String newResource = resourceString;
       newResource = newResource.replace("OBJECT", allObjects.get(i)).replace("LOCATION", allObjectLocations.get(i));
       System.out.println(newResource);
       writer.append(newResource);
       writer.append("\n");
    }
 }
 }