package util;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import util.Material;
import org.joml.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import com.jogamp.opengl.util.GLBuffers;

public class ObjectInstance
{
	enum Buffer_IDs {PositionArrayBuffer,NormalArrayBuffer,TexCoordArrayBuffer,IndexArrayBuffer,NumBuffers};
	//enum Buffer_IDs {PositionArrayBuffer,IndexArrayBuffer,NumBuffers};
	protected IntBuffer vao; //our VAO
	protected IntBuffer vbo;//all our Vertex Buffer Object IDs
	protected util.PolygonMesh mesh;

	protected String name; //a unique "name" for this object
	protected int primitive;



	public ObjectInstance(GL3 gl, String name)
	{
		vao = IntBuffer.allocate(1);
		vbo = IntBuffer.allocate(Buffer_IDs.NumBuffers.ordinal());
		//create the VAO ID for this object
		gl.glGenVertexArrays(1,vao);
		//bind the VAO
		gl.glBindVertexArray(vao.get(0));
		//create as many VBO IDs as you need, in this case 2
		gl.glGenBuffers(Buffer_IDs.NumBuffers.ordinal(),vbo);
		//set the name
		setName(name);
		//default material

	}

	public ObjectInstance(GL3 gl,util.ShaderProgram program,util.PolygonMesh mesh,String name)
	{
        vao = IntBuffer.allocate(1);
        vbo = IntBuffer.allocate(Buffer_IDs.NumBuffers.ordinal());

        //create the VAO ID for this object
		gl.glGenVertexArrays(1,vao);
		//bind the VAO
		gl.glBindVertexArray(vao.get(0));
		//create as many VBO IDs as you need, in this case 2
		gl.glGenBuffers(Buffer_IDs.NumBuffers.ordinal(), vbo);
		//set the name
		setName(name);
		//default material

        initPolygonMesh(gl,program,mesh);
	}

	protected void initPolygonMesh(GL3 gl,util.ShaderProgram program,util.PolygonMesh mesh) {
        int i, j;
        FloatBuffer vertexBuffer = mesh.getVertexPositions();
        FloatBuffer normalBuffer = mesh.getVertexNormals();
        FloatBuffer texcoordsBuffer = mesh.getTexCoords();
        IntBuffer indexBuffer = mesh.getPrimitives();

        this.mesh = mesh;

        this.mesh.computeBoundingBox();

        primitive = mesh.getPrimitiveType();


		/*
		 *Bind the VAO as the current VAO, so that all subsequent commands affect it
		 */
        gl.glBindVertexArray(vao.get(0));

		/*
		 *Allocate the VBO for vertex data and send it to the GPU
		 */
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo.get(Buffer_IDs.PositionArrayBuffer.ordinal()));
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, vertexBuffer.capacity() * GLBuffers.SIZEOF_FLOAT, vertexBuffer, GL3.GL_STATIC_DRAW);

        if (normalBuffer.capacity() > 0) {
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo.get(Buffer_IDs.NormalArrayBuffer.ordinal()));
            gl.glBufferData(GL3.GL_ARRAY_BUFFER, normalBuffer.capacity() * GLBuffers.SIZEOF_FLOAT, normalBuffer, GL3.GL_STATIC_DRAW);
        }


        if (texcoordsBuffer.capacity() > 0)
        {
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER,vbo.get(Buffer_IDs.TexCoordArrayBuffer.ordinal()));
            gl.glBufferData(GL3.GL_ARRAY_BUFFER, texcoordsBuffer.capacity()* GLBuffers.SIZEOF_FLOAT, texcoordsBuffer, GL3.GL_STATIC_DRAW);
        }



		/*
		 *Allocate the VBO for triangle indices and send it to GPU
		 */
		gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, vbo.get(Buffer_IDs.IndexArrayBuffer.ordinal()));
		gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * GLBuffers.SIZEOF_INT, indexBuffer, GL3.GL_STATIC_DRAW);


		program.enable(gl);
		int vPositionLocation = program.getAttributeLocation(gl,"vPosition");
		int vNormalLocation = program.getAttributeLocation(gl,"vNormal");
		int vTexCoordLocation = program.getAttributeLocation(gl,"vTexCoord");
		/*
		 *Specify all the vertex attribute pointers, i.e. tell OpenGL how to organize data according to attributes rather than vertices
		 */

		if (vPositionLocation>=0)
		{
			gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo.get(Buffer_IDs.PositionArrayBuffer.ordinal()));
			gl.glVertexAttribPointer(vPositionLocation, 4, GL3.GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(vPositionLocation);
		}


		if (vNormalLocation>=0)
		{
			gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo.get(Buffer_IDs.NormalArrayBuffer.ordinal()));
			gl.glVertexAttribPointer(vNormalLocation, 4, GL3.GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(vNormalLocation);
		}

		if (vTexCoordLocation>=0)
		{
			gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo.get(Buffer_IDs.TexCoordArrayBuffer.ordinal()));
			gl.glVertexAttribPointer(vTexCoordLocation, 4, GL3.GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(vTexCoordLocation);
		}



		/*
		 *Unbind the VAO to prevent accidental change to all the settings
		 *so at this point, this VAO has two VBOs and two enabled VertexAttribPointers.
		 * It is going to remember all of that!
		 */
		gl.glBindVertexArray(0);
		program.disable(gl);
	}

	/*
	 *The destructor is virtual so that any extra memory allocated by subclasses can be freed
	 *in their respective destructors
	 */
	public void cleanup(GLAutoDrawable gla)
	{
		GL3 gl = gla.getGL().getGL3();
		if (vao.get(0)!=0)
		{
			//give back the VBO IDs to OpenGL, so that they can be reused
			gl.glDeleteBuffers(Buffer_IDs.NumBuffers.ordinal(),vbo);
			//give back the VAO ID to OpenGL, so that it can be reused
			gl.glDeleteVertexArrays(1,vao);
		}
	}

	public void draw(GLAutoDrawable gla)
	{
		GL3 gl = gla.getGL().getGL3();
		//draw the object


		//1. bind its VAO
		gl.glBindVertexArray(vao.get(0));

		//2. execute the "superpower" command
		gl.glDrawElements(primitive, mesh.getPrimitiveCount(), GL.GL_UNSIGNED_INT, 0);

		gl.glBindVertexArray(0);
	}

	public PolygonMesh getMesh()
	{
		return mesh;
	}


	/*
	 *Set the name of this object
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/*
	 *Gets the name of this object
	*/
	public String getName()
	{
		return name;
	}


	public Vector4f getMinimumWorldBounds()
	{
		return mesh.getMinimumBounds();
	}

	public Vector4f getMaximumWorldBounds()
	{
		return mesh.getMaximumBounds();
	}

}
