package sgraph.Nodes;

import com.jogamp.opengl.util.texture.Texture;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import sgraph.IScenegraph;
import sgraph.IScenegraphRenderer;
import util.Light;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * This node represents the leaf of a scene graph. It is the only type of node that has
 * actual geometry to render.
 *
 * @author Amit Shesh
 */
public class LeafNode extends AbstractNode {
  /**
   * The name of the object instance that this leaf contains. All object instances are stored
   * in the scene graph itself, so that an instance can be reused in several leaves
   */
  protected String objInstanceName;
  /**
   * The material associated with the object instance at this leaf
   */
  protected util.Material material;

  /**
   * The texture associated with the object instance at this leaf
   */
  protected Texture texture;

  private String texturename;

  public LeafNode(String instanceOf, IScenegraph graph, String name, String texturename) {
    super(graph, name);
    this.objInstanceName = instanceOf;
    this.texturename = texturename;
  }


  /*
 *Set the material of each vertex in this object
 */
  @Override
  public void setMaterial(util.Material mat) {
    material = new util.Material(mat);
  }

  @Override
  public void setTexture(Texture t) throws IllegalArgumentException {
    this.texture = t;
  }

  @Override
  public Matrix4f getObjectToViewTransform(Matrix4f accumulator, Matrix4f worldToView) {

    if (this.parent == null) {
      return new Matrix4f().mul(worldToView).mul(accumulator);
    }

    // M v <- w     M w <- train    M train <- animation    M animation <- transform    M transform <- light
    return parent.getObjectToViewTransform(accumulator, worldToView);
  }

  /*
   * gets the material
   */
  public util.Material getMaterial() {
    return material;
  }

  @Override
  public INode clone() {
    LeafNode newclone = new LeafNode(this.objInstanceName, scenegraph, name, this.texturename);
    newclone.setMaterial(this.getMaterial());
    newclone.setTexture(this.texture);

    for (Light l : listOfLights) {
      newclone.addLight(l.clone());
    }
    return newclone;
  }


  /**
   * Delegates to the scene graph for rendering. This has two advantages:
   * <ul>
   * <li>It keeps the leaf light.</li>
   * <li>It abstracts the actual drawing to the specific implementation of the scene graph renderer</li>
   * </ul>
   *
   * @param context   the generic renderer context {@link sgraph.IScenegraphRenderer}
   * @param modelView the stack of modelview matrices
   * @throws IllegalArgumentException
   */
  @Override
  public void draw(IScenegraphRenderer context, Stack<Matrix4f> modelView) throws IllegalArgumentException {
    if (objInstanceName.length() > 0) {
      context.drawMesh(objInstanceName, material, modelView.peek(), this.texturename);
    }
  }

  @Override
  public List<Light> getAllLightsHelp(List<Light> accLights, List<INode> unvisited, Matrix4f worldToView) {
    // Transform New Lights in this node
//        System.out.println("Leaf getAllLights");
    Matrix4f toView = getObjectToViewTransform(new Matrix4f(), new Matrix4f(worldToView));
    List<Light> listOfLightsTransformed = new ArrayList<>();
    for (Light l : listOfLights) {
      listOfLightsTransformed.add(l.clone());
    }

    transformLightsPassedIn(toView, listOfLightsTransformed);

    // New Acc
    List<Light> transformedLights = new ArrayList<>(accLights);
    transformedLights.addAll(listOfLightsTransformed);

    if (!unvisited.isEmpty()) {
      unvisited.remove(0);
    }

    if (unvisited.isEmpty()) {
      return transformedLights;
    }
    return unvisited.get(0).getAllLightsHelp(transformedLights, unvisited, worldToView);
  }


}
