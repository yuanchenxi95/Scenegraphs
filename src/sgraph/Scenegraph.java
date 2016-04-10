package sgraph;

import org.joml.Matrix4f;
import sgraph.Nodes.INode;
import util.Light;
import util.PolygonMesh;
import util.TextureImage;

import java.util.*;

/**
 * A specific implementation of this scene graph. This implementation is still independent
 * of the rendering technology (i.e. OpenGL)
 * @author Amit Shesh
 */
public class Scenegraph implements IScenegraph
{
    /**
     * The root of the scene graph tree
     */
    protected INode root;

    /**
     * A map to store the (name,mesh) pairs. A map is chosen for efficient search
     */
    protected Map<String,util.PolygonMesh> meshes;

    /**
     * A map to store the (name,textureImage) pairs. A map is chose for efficient search
     */
    protected Map<String, util.TextureImage> textureImages;

    /**
     * A map to store the (name,node) pairs. A map is chosen for efficient search
     */
    protected Map<String,INode> nodes;

    /**
     * The associated renderer for this scene graph. This must be set before attempting to
     * render the scene graph
     */
    protected IScenegraphRenderer renderer;


    public Scenegraph()
    {
        root = null;
        meshes = new TreeMap<String,util.PolygonMesh>();
        nodes = new TreeMap<String,INode>();
        textureImages = new TreeMap<String, TextureImage>();
    }

    public void dispose()
    {
        renderer.dispose();
    }

    /**
     * Sets the renderer, and then adds all the meshes to the renderer.
     * This function must be called when the scene graph is complete, otherwise not all of its
     * meshes will be known to the renderer
     * @param renderer The {@link IScenegraphRenderer} object that will act as its renderer
     * @throws Exception
     */
    @Override
    public void setRenderer(IScenegraphRenderer renderer) throws Exception {
        this.renderer = renderer;
        List<Light> lol = this.getRoot().getAllLights(new Matrix4f());
        this.renderer.addLights(lol);

        //now add all the meshes
        for (String meshName:meshes.keySet())
        {
            this.renderer.addMesh(meshName,meshes.get(meshName));
        }
        for (String texName : textureImages.keySet()) {
            System.out.println();
            this.renderer.addTexture(texName, textureImages.get(texName));
        }



    }


    /**
     * Set the root of the scenegraph, and then pass a reference to this scene graph object
     * to all its node. This will enable any node to call functions of its associated scene graph
     * @param root
     */

    @Override
    public void makeScenegraph(INode root)
    {
        this.root = root;
        this.root.setScenegraph(this);

    }

    /**
     * Draw this scene graph. It delegates this operation to the renderer
     * @param modelView
     */
    @Override
    public void draw(Stack<Matrix4f> modelView) {



        if ((root!=null) && (renderer!=null))
        {
            renderer.draw(root,modelView);
        }
    }


    @Override
    public void addPolygonMesh(String name,util.PolygonMesh mesh)
    {
        meshes.put(name,mesh);
    }

    @Override
    public void addTextureImage(String name, TextureImage textureImage) {
        textureImages.put(name, textureImage);
    }

    boolean trainTransformFlag = true;

    // given time from 0 - 360
    @Override
    public void animate(float time) {

        float radius = 300f;
        //////////////////////////////////////
        // for test head part light
        if (trainTransformFlag) {
            if (time * 2 == 718) {
                trainTransformFlag = false;
            }
            nodes.get("train-transform").setAnimationTransform(
                new Matrix4f().translate(time * 2, 0, 0));
        } else {
            if (time * 2 == 718) {
                trainTransformFlag = true;
            }
            nodes.get("train-transform").setAnimationTransform(
                    new Matrix4f().translate(720 - time * 2, 0, 0));
        }

//        nodes.get("train-transform").setAnimationTransform(
//                new Matrix4f().rotate((float) Math.toRadians(time), 0, 1, 0));
//                new Matrix4f().translate(0.5f * time, 0, 0));

        //////////////////////////////////////


        float offset = (float) (Math.PI / 2);

        nodes.get("spiderB-transform").setAnimationTransform(new Matrix4f()
                .rotate((float) Math.toRadians(time) + offset, 0, 1, 0)
                .translate(radius, 0, 0)
                .rotate((float) Math.toRadians(-90), 0, 1, 0));

        nodes.get("spiderA-transform").setAnimationTransform(new Matrix4f()
                .rotate((float) Math.toRadians(time), 0, 1, 0)
                .translate(radius, 0, 0)
                .rotate((float) Math.toRadians(-90), 0, 1, 0));
        // print all the names in the map
//        for(INode n : nodes.values())
//            System.out.println(n.getName());

        animateSpiderLegs();


    }

    // Current time count
    int count = 0;

    // Maximum time count before returning to 0
    int loopLimit = 100;

    // Time offset betwen each leg of rows
    int offset = 20;

    /**
     * Animate the movement of spider legs
     */
    private void animateSpiderLegs() {

        count++;
        // Animation Percentages from 0 to 1.
        float row0_percentage = (count % loopLimit) / (float) loopLimit;
        float row1_percentage = ((count + offset) % loopLimit) / (float) loopLimit;
        float row2_percentage = ((count + offset * 2) % loopLimit) / (float) loopLimit;
        float row3_percentage = ((count + offset * 3) % loopLimit) / (float) loopLimit;
        // Using equation: rotationAmount = 1/8 * sin(theta * 360 - 180)
        // Both spiders' Left Side
        nodes.get("spiderA-root-legLeft0").setAnimationTransform(new Matrix4f()
                .rotate((float) Math.sin(Math.toRadians(row0_percentage * 360 - 180)) / 8f, 0, 1, 0));
        nodes.get("spiderA-root-legLeft1").setAnimationTransform(new Matrix4f()
                .rotate((float) Math.sin(Math.toRadians(row1_percentage * 360 - 180)) / 8f, 0, 1, 0));
        nodes.get("spiderA-root-legLeft2").setAnimationTransform(new Matrix4f()
                .rotate((float) Math.sin(Math.toRadians(row2_percentage * 360 - 180)) / 8f, 0, 1, 0));
        nodes.get("spiderA-root-legLeft3").setAnimationTransform(new Matrix4f()
                .rotate((float) Math.sin(Math.toRadians(row3_percentage * 360 - 180)) / 8f, 0, 1, 0));

        nodes.get("spiderB-root-legLeft0").setAnimationTransform(new Matrix4f()
                .rotate((float) Math.sin(Math.toRadians(row0_percentage * 360 - 180)) / 8f, 0, 1, 0));
        nodes.get("spiderB-root-legLeft1").setAnimationTransform(new Matrix4f()
                .rotate((float) Math.sin(Math.toRadians(row1_percentage * 360 - 180)) / 8f, 0, 1, 0));
        nodes.get("spiderB-root-legLeft2").setAnimationTransform(new Matrix4f()
                .rotate((float) Math.sin(Math.toRadians(row2_percentage * 360 - 180)) / 8f, 0, 1, 0));
        nodes.get("spiderB-root-legLeft3").setAnimationTransform(new Matrix4f()
                .rotate((float) Math.sin(Math.toRadians(row3_percentage * 360 - 180)) / 8f, 0, 1, 0));

        // Both spiders' Right side
        nodes.get("spiderA-root-legRight0").setAnimationTransform(new Matrix4f()
                .rotate((float) Math.sin(Math.toRadians(row0_percentage * 360 - 180)) / 8f, 0, 1, 0));
        nodes.get("spiderA-root-legRight1").setAnimationTransform(new Matrix4f()
                .rotate((float) Math.sin(Math.toRadians(row1_percentage * 360 - 180)) / 8f, 0, 1, 0));
        nodes.get("spiderA-root-legRight2").setAnimationTransform(new Matrix4f()
                .rotate((float) Math.sin(Math.toRadians(row2_percentage * 360 - 180)) / 8f, 0, 1, 0));
        nodes.get("spiderA-root-legLeft3").setAnimationTransform(new Matrix4f()
                .rotate((float) Math.sin(Math.toRadians(row3_percentage * 360 - 180)) / 8f, 0, 1, 0));

        nodes.get("spiderB-root-legRight0").setAnimationTransform(new Matrix4f()
                .rotate((float) Math.sin(Math.toRadians(row0_percentage * 360 - 180)) / 8f, 0, 1, 0));
        nodes.get("spiderB-root-legRight1").setAnimationTransform(new Matrix4f()
                .rotate((float) Math.sin(Math.toRadians(row1_percentage * 360 - 180)) / 8f, 0, 1, 0));
        nodes.get("spiderB-root-legRight2").setAnimationTransform(new Matrix4f()
                .rotate((float) Math.sin(Math.toRadians(row2_percentage * 360 - 180)) / 8f, 0, 1, 0));
        nodes.get("spiderB-root-legRight3").setAnimationTransform(new Matrix4f()
                .rotate((float) Math.sin(Math.toRadians(row3_percentage * 360 - 180)) / 8f, 0, 1, 0));
    }

    @Override
    public void addNode(String name, INode node) {
        nodes.put(name,node);
    }


    @Override
    public INode getRoot() {
        return root;
    }

    @Override
    public Map<String, PolygonMesh> getPolygonMeshes() {
        Map<String,util.PolygonMesh> meshes = new TreeMap<String,PolygonMesh>();

        meshes.putAll(this.meshes);
        return meshes;
    }

    @Override
    public Map<String, INode> getNodes() {
        Map<String,INode> nodes = new TreeMap<String,INode>();
        nodes.putAll(this.nodes);
        return nodes;
    }


}
