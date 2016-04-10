package sgraph;

import org.joml.Matrix4f;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import sgraph.Nodes.GroupNode;
import sgraph.Nodes.INode;
import sgraph.Nodes.LeafNode;
import sgraph.Nodes.TransformNode;
import util.TextureImage;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;
import java.util.TreeMap;


/**
 * A SAX parser for parsing the scene graph and compiling an {@link sgraph.IScenegraph} object
 * from it.
 * @author Amit Shesh
 */
public class SceneXMLReader
{
    public static IScenegraph importScenegraph(InputStream in) throws Exception
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser=null;
        IScenegraph scenegraph = null;

        parser = factory.newSAXParser();

        MyHandler handler = new MyHandler();
        parser.parse(in,handler);

        scenegraph = handler.getScenegraph();
        return scenegraph;
    }
}

class MyHandler extends DefaultHandler
{
    private IScenegraph scenegraph;
    private INode node;
    private Stack<INode> stackNodes;
    private String data;
    private Matrix4f transform;
    private util.Material material;
    private util.Light light;
    private Map<String,INode> subgraph;

    public IScenegraph getScenegraph()
    {
        return scenegraph;
    }


    @Override
    public void startDocument() throws SAXException
    {
        System.out.println("Parsing started");
        node = null;
        stackNodes = new Stack<INode>();
        scenegraph = new Scenegraph();
        subgraph = new TreeMap<String,INode>();
        transform = new Matrix4f();
        material = new util.Material();
        light = new util.Light();
    }

    public void endDocument() throws SAXException
    {
    }

    public void startElement(String uri,String localName,String qName,Attributes attributes) throws SAXException
    {
        System.out.println("Start tag: "+qName);
        switch (qName)
        {
            case "scene":
            {
                stackNodes.push(new GroupNode(scenegraph,"Root of scene graph"));
                subgraph.put(stackNodes.peek().getName(),stackNodes.peek());
            }
            break;
            case "group":
            {
                String name = "";
                String copyof = "";
                String fromfile="";
                for (int i=0;i<attributes.getLength();i++)
                {
                    if (attributes.getQName(i).equals("name"))
                        name = attributes.getValue(i);
                    else if (attributes.getQName(i).equals("copyof"))
                        copyof = attributes.getValue(i);
                    else if (attributes.getQName(i).equals("from"))
                        fromfile = attributes.getValue(i);
                }
                if ((copyof.length()>0) && (subgraph.containsKey(copyof)))
                {
                    node = subgraph.get(copyof).clone();
                    node.setName(name);
                }
                else if (fromfile.length()>0)
                {
                    sgraph.IScenegraph tempsg=null;
                    try {
                        tempsg = SceneXMLReader.importScenegraph(getClass().getClassLoader().getResourceAsStream(fromfile));
                    } catch (Exception e) {
                        throw new SAXException(e.getMessage());
                    }
                    node = new GroupNode(scenegraph,name);

                    for (Map.Entry<String,util.PolygonMesh> s:tempsg.getPolygonMeshes().entrySet())
                    {
                        scenegraph.addPolygonMesh(s.getKey(),s.getValue());
                    }
                    //rename all the nodes in tempsg to prepend with the name of the group node
                    Map<String,INode> nodes = tempsg.getNodes();
                    for (Map.Entry<String,INode> s:nodes.entrySet())
                    {
                        s.getValue().setName(name+"-"+s.getValue().getName());
                        scenegraph.addNode(s.getValue().getName(),s.getValue());
                    }

                    node.addChild(tempsg.getRoot());
                }
                else
                    node = new GroupNode(scenegraph,name);
                try
                {
                    stackNodes.peek().addChild(node);
                }
                catch (IllegalArgumentException e)
                {
                    throw new SAXException(e.getMessage());
                }
                stackNodes.push(node);
                subgraph.put(stackNodes.peek().getName(),stackNodes.peek());
            }
            break;
            case "transform":
            {
                String name = "";
                for (int i=0;i<attributes.getLength();i++)
                {
                    if (attributes.getQName(i).equals("name"))
                        name = attributes.getValue(i);
                }
                node = new TransformNode(scenegraph,name);
                try
                {
                    stackNodes.peek().addChild(node);
                }
                catch (IllegalArgumentException e)
                {
                    throw new SAXException(e.getMessage());
                }
                transform.identity();
                stackNodes.push(node);
                subgraph.put(stackNodes.peek().getName(),stackNodes.peek());
            }
            break;
            case "object":
            {
                String name = "";
                String objectname ="";
                String texturename = "";
                for (int i=0;i<attributes.getLength();i++)
                {
                    if (attributes.getQName(i).equals("name"))
                    {
                        name = attributes.getValue(i);
                    }
                    else if (attributes.getQName(i).equals("instanceof"))
                    {
                        objectname = attributes.getValue(i);
                    } else if (attributes.getQName(i).equals("texture"))
                    {
                        texturename = attributes.getValue(i);
                    }
                }
                if (objectname.length()>0)
                {

                    node = new LeafNode(objectname,scenegraph,name,texturename);
                    try
                    {
                        stackNodes.peek().addChild(node);
                    }
                    catch (IllegalArgumentException e)
                    {
                        throw new SAXException(e.getMessage());
                    }
                    stackNodes.push(node);
                    subgraph.put(stackNodes.peek().getName(),stackNodes.peek());
                }
            }
            break;
            case "instance":
            {
                String name = "";
                String path = "";
                for (int i=0;i<attributes.getLength();i++)
                {
                    if (attributes.getQName(i).equals("name"))
                    {
                        name = attributes.getValue(i);
                    }
                    else if (attributes.getQName(i).equals("path"))
                    {
                        path = attributes.getValue(i);
                        if (!path.endsWith(".obj"))
                            path = path + ".obj";
                    }
                }
                if ((name.length()>0) && (path.length()>0))
                {
                    util.PolygonMesh mesh=null;
                    mesh = util.ObjImporter.importFile(getClass().getClassLoader().getResourceAsStream(path), false);
                    scenegraph.addPolygonMesh(name,mesh);
                }

            }
            break;
            case "image":{
                String name = "";
                String path = "";
                for (int i=0;i<attributes.getLength();i++)
                {
                    if (attributes.getQName(i).equals("name"))
                    {
                        name = attributes.getValue(i);
                    }
                    else if (attributes.getQName(i).equals("path"))
                    {
                        path = attributes.getValue(i);
                    }
                }
                if ((name.length()>0) && (path.length()>0))
                {
                    util.TextureImage tex;
                    try
                    {

                        tex = new TextureImage(path, name,
                                path.substring(path.length()-3));
                    }
                    catch (IOException e)
                    {
                        throw new SAXException(e.getMessage());
                    }

                    scenegraph.addTextureImage(name,tex);
                }
            }
            break;

        }
        data = "";
    }

    public void endElement(String uri,String localName,String qName) throws SAXException
    {
        float f1;
        float f2;
        float f3;

        Scanner sc;
        System.out.println("End tag: "+qName);

        switch (qName)
        {
            case "scene":
                if (stackNodes.peek().getName().equals("Root of scene graph"))
                    scenegraph.makeScenegraph(stackNodes.peek());
                else
                    throw new SAXException("Invalid scene file");
                break;
            case "group":
            case "transform":
            case "object":
                stackNodes.pop();
                break;
            case "set":
                stackNodes.peek().setTransform(transform);
                transform.identity();
                break;
            case "scale":
                sc = new Scanner(data);
                transform.scale(sc.nextFloat(),sc.nextFloat(),sc.nextFloat());
                break;
            case "rotate":
                sc = new Scanner(data);
                transform.rotate((float) Math.toRadians(sc.nextFloat()), sc.nextFloat(), sc.nextFloat(), sc.nextFloat());
                break;
            case "translate":
                sc = new Scanner(data);
                transform.translate(sc.nextFloat(),sc.nextFloat(),sc.nextFloat());
                break;
            case "material":
                stackNodes.peek().setMaterial(material);
                // wipe out the data stored in light and material
                material = new util.Material();
                light = new util.Light();
                break;
            case "light":
                stackNodes.peek().addLight(light);
                // wipe out the data stored in light and material
                material = new util.Material();
                light = new util.Light();
                break;
            case "color":
                sc = new Scanner(data);
                material.setAmbient(sc.nextFloat(),sc.nextFloat(),sc.nextFloat());
                material.setDiffuse(material.getAmbient());
                material.setSpecular(material.getAmbient());
                material.setShininess(1.0f);
                break;
            case "ambient":
                sc = new Scanner(data);
                f1 = sc.nextFloat();
                f2 = sc.nextFloat();
                f3 = sc.nextFloat();
                material.setAmbient(f1, f2, f3);
                light.setAmbient(f1, f2, f3);
                break;
            case "diffuse":
                sc = new Scanner(data);
                f1 = sc.nextFloat();
                f2 = sc.nextFloat();
                f3 = sc.nextFloat();
                material.setDiffuse(f1, f2, f3);
                light.setDiffuse(f1, f2, f3);
                break;
            case "specular":
                sc = new Scanner(data);
                f1 = sc.nextFloat();
                f2 = sc.nextFloat();
                f3 = sc.nextFloat();
                material.setSpecular(f1, f2, f3);
                light.setSpecular(f1, f2, f3);
                break;
            case "emissive":
                sc = new Scanner(data);
                material.setEmission(sc.nextFloat(),sc.nextFloat(),sc.nextFloat());
                break;
            case "shininess":
                sc = new Scanner(data);
                material.setShininess(sc.nextFloat());
                break;
            case "absorption":
                sc = new Scanner(data);
                material.setAbsorption(sc.nextFloat());
                break;
            case "reflection":
                sc = new Scanner(data);
                material.setReflection(sc.nextFloat());
                break;
            case "transparency":
                sc = new Scanner(data);
                material.setTransparency(sc.nextFloat());
                break;
            case "refractive":
                sc = new Scanner(data);
                material.setRefractiveIndex(sc.nextFloat());
                break;
            case "position":
                sc = new Scanner(data);
                light.setPosition(sc.nextFloat(), sc.nextFloat(), sc.nextFloat());
                break;
            case "direction":
                sc = new Scanner(data);
                light.setDirection(sc.nextFloat(), sc.nextFloat(), sc.nextFloat());
                break;
            case "spotangle":
                sc = new Scanner(data);
                light.setSpotAngle(sc.nextFloat());
                break;
            case "spotdirection":
                sc = new Scanner(data);
                light.setSpotDirection(sc.nextFloat(), sc.nextFloat(), sc.nextFloat());
                break;
        }


        data = "";
    }

    public void characters(char ch[], int start, int length) throws SAXException
    {
        if (data.length()>0)
            data = data + " " + new String(ch,start,length);
        else
            data = new String(ch,start,length);
    }

}





