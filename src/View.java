import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import org.joml.*;
import sgraph.Nodes.INode;


import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Stack;


/**
 * Created by ashesh on 9/18/2015.
 *
 * The View class is the "controller" of all our OpenGL stuff. It cleanly encapsulates all our OpenGL functionality from the rest of Java GUI, managed
 * by the JOGLFrame class.
 */
public class View
{
    private int WINDOW_WIDTH,WINDOW_HEIGHT;
    private Stack<Matrix4f> modelView;
    private Matrix4f projection,trackballTransform;
    private Matrix3f keyBoardTransform;
    private float trackballRadius;
    private Vector2f mousePos;
    private util.ObjectInstance meshObject;


    private util.ShaderProgram program;
    private int projectionLocation;
    private sgraph.IScenegraph scenegraph;
    private int angleOfRotation;

    private HashMap<Character, Boolean> cameraMoveCharMap;


    //INVARIANT lookAtPositionInit.equals(cameraPositionInit)
    private final Vector3f cameraPositionInit = new Vector3f(0, 300, 400);

    private Vector3f cameraPosition = new Vector3f(cameraPositionInit);
    Vector3f upVector;



    private enum cameraState {
        STATIONARY,
        ONSPIDER,
        KEYCONTROL,
    }

    private cameraState cState= cameraState.KEYCONTROL;



    public View()
    {
        projection = new Matrix4f();
        modelView = new Stack<Matrix4f>();
        angleOfRotation = 0;
        trackballRadius = 300;


        resetMoveCamera();


    }

    private void resetMoveCamera() {
        // initialize keyboard control
        cameraMoveCharMap = new HashMap<Character, Boolean>();
        cameraMoveCharMap.put('w',false);
        cameraMoveCharMap.put('s',false);
        cameraMoveCharMap.put('a',false);
        cameraMoveCharMap.put('d',false);
        keyBoardTransform = new Matrix3f();
        upVector = new Vector3f(0, 1,0);
        trackballTransform = new Matrix4f();

    }


    public void initScenegraph(GLAutoDrawable gla,InputStream in) throws Exception
    {
        GL3 gl = gla.getGL().getGL3();

        if (scenegraph!=null)
            scenegraph.dispose();

        scenegraph = sgraph.SceneXMLReader.importScenegraph(in);

        sgraph.IScenegraphRenderer renderer = new sgraph.GL3ScenegraphRenderer();
        renderer.setContext(gla);
        renderer.initShaderProgram(program);
        scenegraph.setRenderer(renderer);
    }

    public void init(GLAutoDrawable gla) throws Exception
    {
        GL3 gl = gla.getGL().getGL3();




        //compile and make our shader program. Look at the ShaderProgram class for details on how this is done
        program = new util.ShaderProgram();

//        program.createProgram(gl,"shaders/phong-multiple.vert","shaders/phong-multiple.frag");
//        program.createProgram(gl,"shaders/outline.vert","shaders/outline.frag");
        program.createProgram(gl,"shaders/toonShading.vert","shaders/toonShading.frag");


        //get input variables that need to be given to the shader program
        projectionLocation = program.getUniformLocation(gl,"projection");
    }



    public void draw(GLAutoDrawable gla)
    {
        scenegraph.animate(angleOfRotation);
        angleOfRotation = (angleOfRotation+1)%360;




        GL3 gl = gla.getGL().getGL3();

//        gl.glClearColor(1, 1, 1, 1); // White
        gl.glClearColor(0, 0, 0, 1); // Black
        gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
        gl.glEnable(GL.GL_DEPTH_TEST);




        program.enable(gl);

        while (!modelView.empty())
            modelView.pop();

        /*
         *In order to change the shape of this triangle, we can either move the vertex positions above, or "transform" them
         * We use a modelview matrix to store the transformations to be applied to our triangle.
         * Right now this matrix is identity, which means "no transformations"
         */
        modelView.push(new Matrix4f());





        switch (cState) {
            case STATIONARY:
                modelView.peek().lookAt(new Vector3f(0,500,500),new Vector3f(0,50,0),new Vector3f(0,1,0))
                        .mul(trackballTransform);
                break;
            case ONSPIDER:

                // camera matrix
                Matrix4f cameraMatrix = new Matrix4f(modelView.peek());

                INode n = scenegraph.getNodes().get("spiderA-root-spiderEye");
                if (n == null) {
                    throw new NullPointerException("Cannot find the head");
                }
                Matrix4f om = new Matrix4f().identity();
                Matrix4f objectToView = n.getObjectToViewTransform(new Matrix4f(), new Matrix4f(cameraMatrix));
                Matrix4f viewToObject = new Matrix4f(objectToView).invert();

                modelView.peek()
                        .lookAt(new Vector3f(-1,0,0),new Vector3f(-2,0,0),new Vector3f(0,1,0))
                        .mul(viewToObject);
                break;
            case KEYCONTROL:

                moveCamera();

                // Don't Change anything
                Vector3f rotate = new Vector3f(0,0,-1).mul(new Matrix3f(keyBoardTransform));
                upVector = new Vector3f(0,1,0).mul(new Matrix3f(keyBoardTransform));

                // System.out.println(rotate.dot(upVector));

                modelView.peek().lookAt(cameraPosition,new Vector3f(cameraPosition).add(rotate), upVector);



                break;
            default:
                throw new IllegalArgumentException("camera state is wrong");
        }








//        System.out.println("End camera");




    /*
     *Supply the shader with all the matrices it expects.
    */
        FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
        gl.glUniformMatrix4fv(projectionLocation,1,false,projection.get(fb));
        //return;


//        gl.glPolygonMode(GL.GL_FRONT_AND_BACK,GL3.GL_LINE); //OUTLINES
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK,GL3.GL_FILL); //FILLED

        scenegraph.draw(modelView);
    /*
     *OpenGL batch-processes all its OpenGL commands.
          *  *The next command asks OpenGL to "empty" its batch of issued commands, i.e. draw
     *
     *This a non-blocking function. That is, it will signal OpenGL to draw, but won't wait for it to
     *finish drawing.
     *
     *If you would like OpenGL to start drawing and wait until it is done, call glFinish() instead.
     */

        gl.glFlush();

        program.disable(gl);



    }


    public void keyTyped(char c) {
        if (c == 'r') {
            trackballTransform = new Matrix4f().identity();


           cameraPosition = new Vector3f(cameraPositionInit);
            resetMoveCamera();
        }

        if (c == 'c') {
            if (cState == cameraState.ONSPIDER) {
                cState = cameraState.KEYCONTROL;

            } else if (cState == cameraState.STATIONARY) {
                cState = cameraState.ONSPIDER;
            } else if (cState == cameraState.KEYCONTROL) {
                cState = cameraState.STATIONARY;
            }
        }



    }

    public void keyPressed(char c) {
        System.out.println("press " + c);
        if (cameraState.KEYCONTROL == cState) {
            if (c == 'w' || c == 's' || c == 'a' || c == 'd') {
                cameraMoveCharMap.put(c, true);
            }
        }
    }

    public void keyReleased(char c) {
        System.out.println("release " + c);
        if (cameraState.KEYCONTROL == cState) {
            if (c == 'w' || c == 's' || c == 'a' || c == 'd') {
                cameraMoveCharMap.put(c, false);
            }
        }


    }

    private void moveCamera() {

        if (cState != cameraState.KEYCONTROL) return;

        for (Character cameraMoveChar : cameraMoveCharMap.keySet()) {

            if (cameraMoveCharMap.get('w')) {

                Vector3f temp = new Vector3f(0, 0, -4);
                temp = temp.mul(keyBoardTransform);

                cameraPosition.add(temp);
            }
            if (cameraMoveCharMap.get('s')) {
                Vector3f temp = new Vector3f(0, 0, 4);
                temp = temp.mul(keyBoardTransform);

                cameraPosition.add(temp);
            }
            if (cameraMoveCharMap.get('a')) {
                Vector3f temp = new Vector3f(-4, 0, 0);
                temp = temp.mul(keyBoardTransform);

                cameraPosition.add(temp);
            }
            if (cameraMoveCharMap.get('d')) {
                Vector3f temp = new Vector3f(4, 0, 0);
                temp = temp.mul(keyBoardTransform);

                cameraPosition.add(temp);
            }
        }



    }



    public void mousePressed(int x,int y)
    {
        mousePos = new Vector2f(x,y);
    }

    public void mouseReleased(int x,int y)
    {
        System.out.println("Released");
    }

    public void mouseDragged(int x,int y)
    {
        if (cState != cameraState.ONSPIDER) {
            Vector2f newM = new Vector2f(x, y);

            Vector2f delta = new Vector2f(newM.x - mousePos.x, newM.y - mousePos.y);
            mousePos = new Vector2f(newM);

            Matrix4f rotationMatrix = new Matrix4f().rotate(delta.x / trackballRadius, 0, 1, 0)
                    .rotate(delta.y / trackballRadius, 1, 0, 0);

            if (cState == cameraState.STATIONARY) {
                trackballTransform = new Matrix4f()
                        .rotate(delta.x / trackballRadius, 0, 1, 0)
                        .rotate(delta.y / trackballRadius, 1, 0, 0).mul(trackballTransform);
            }

            if (cState == cameraState.KEYCONTROL) {
                keyBoardTransform =
                        new Matrix3f().mul(keyBoardTransform).rotate(-delta.x / trackballRadius, 0, 1, 0)
                        .rotate(-delta.y / trackballRadius, 1, 0, 0);

            }
        }

    }

    public void reshape(GLAutoDrawable gla,int x,int y,int width,int height)
    {
        GL gl = gla.getGL();
        WINDOW_WIDTH = width;
        WINDOW_HEIGHT = height;
        gl.glViewport(0, 0, width, height);

        projection = new Matrix4f().perspective((float)Math.toRadians(120.0f),(float)width/height,0.1f,10000.0f);
//        projection = new Matrix4f().ortho(-400,400,-400,400,0.1f,10000.0f);

    }

    public void dispose(GLAutoDrawable gla)
    {
        GL3 gl = gla.getGL().getGL3();

    }



}
