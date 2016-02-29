package util;

import org.joml.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PolygonMesh
{
    protected List<Vector4f> positions;
    protected List<Vector4f> normals;
    protected List<Vector4f> texcoords;
    protected List<Integer> primitives;
    protected int primitiveType;
    protected int primitiveSize;

    protected Vector4f minBounds,maxBounds; //bounding box

    public PolygonMesh()
    {
        positions = new ArrayList<Vector4f>();
        normals = new ArrayList<Vector4f>();
        texcoords = new ArrayList<Vector4f>();

        primitives = new ArrayList<Integer>();
        primitiveType = primitiveSize = 0;
        minBounds = new Vector4f();
        maxBounds = new Vector4f();
    }

    public void setPrimitiveType(int v)
    {
        primitiveType = v;
    }

    public int getPrimitiveType() { return primitiveType;}

    public void setPrimitiveSize(int s)
    {
        primitiveSize = s;
    }
    public int getPrimitiveSize()
    {
        return primitiveSize;
    }

    public int getPrimitiveCount()
    {
        return primitives.size();
    }

    public int getVertexCount()
    {
        return positions.size();
    }


    public Vector4f getMinimumBounds()
    {
        return new Vector4f(minBounds);
    }

    public Vector4f getMaximumBounds()
    {
        return new Vector4f(maxBounds);
    }

    public FloatBuffer getVertexPositions()
    {
        return convertToFloatBuffer(positions);
    }
    public List<Vector4f> getVertexPositionsAsList(){ return new ArrayList<Vector4f>(positions);}

    public FloatBuffer getVertexNormals()
    {
        return convertToFloatBuffer(normals);
    }
    public List<Vector4f> getVertexNormalsAsList(){ return new ArrayList<Vector4f>(normals);}

    public FloatBuffer getTexCoords()
    {
        return convertToFloatBuffer(texcoords);
    }
    public List<Vector4f> getTexCoordsAsList(){ return new ArrayList<Vector4f>(texcoords);}

    public IntBuffer getPrimitives()
    {
        return convertToIntBuffer(primitives);
    }
    public List<Integer> getPrimitivesAsList() { return new ArrayList<Integer>(primitives);}

    private FloatBuffer convertToFloatBuffer(List<Vector4f> arr)
    {
     /*   FloatBuffer f = FloatBuffer.allocate(4*arr.size());

        for (int i=0;i<arr.size();i++)
        {
            f.put(arr.get(i).x);
            f.put(arr.get(i).y);
            f.put(arr.get(i).z);
            f.put(arr.get(i).w);
        }*/
        float []array = new float[4*arr.size()];
        for (int i=0;i<arr.size();i++)
        {
            array[4*i] = arr.get(i).x;
            array[4*i+1] = arr.get(i).y;
            array[4*i+2] = arr.get(i).z;
            array[4*i+3] = arr.get(i).w;
        }
        FloatBuffer f = FloatBuffer.wrap(array);
        return f;
    }



    private IntBuffer convertToIntBuffer(List<Integer> arr)
    {
        /*IntBuffer f = IntBuffer.allocate(arr.size());

        for (int i=0;i<arr.size();i++)
        {
            f.put(arr.get(i));
        }
        */
        int []array = new int[arr.size()];
        for (int i=0;i<arr.size();i++)
        {
            array[i] = arr.get(i);
        }
        IntBuffer f = IntBuffer.wrap(array);
        return f;
    }


    public void setVertexPositions(List<Vector4f> vp)
    {
        positions = new ArrayList<Vector4f>(vp);
        computeBoundingBox();
    }

    public void setNormals(List<Vector4f> vn) {normals = new ArrayList<Vector4f>(vn);}

    public void setTexcoords(List<Vector4f> vt) {texcoords = new ArrayList<Vector4f>(vt);}

    public void setPrimitives(List<Integer> t)
    {
        primitives = new ArrayList<Integer>(t);
    }


    protected void computeBoundingBox()
    {
        int j;

        if (positions.size()<=0)
            return;

        minBounds = new Vector4f(positions.get(0));
        maxBounds = new Vector4f(positions.get(0));

        for (j=0;j<positions.size();j++)
        {
            Vector4f p = positions.get(j);

            if (p.x<minBounds.x)
            {
                minBounds.x = p.x;
            }

            if (p.x>maxBounds.x)
            {
                maxBounds.x = p.x;
            }

            if (p.y<minBounds.y)
            {
                minBounds.y = p.y;
            }

            if (p.y>maxBounds.y)
            {
                maxBounds.y = p.y;
            }

            if (p.z<minBounds.z)
            {
                minBounds.z = p.z;
            }

            if (p.z>maxBounds.z)
            {
                maxBounds.z = p.z;
            }
        }
    }

    public void computeNormals()
    {
        int i,j,k;

        normals = new ArrayList<Vector4f>();

        for (i=0;i<positions.size();i++)
        {
            normals.add(new Vector4f(0.0f,0.0f,0.0f,0.0f));
        }

        for (i=0;i<primitives.size();i+=primitiveSize)
        {
            Vector4f norm = new Vector4f(0.0f,0.0f,0.0f,0.0f);



            //compute the normal of this triangle
            int []v = new int[primitiveSize];

            for (k=0;k<primitiveSize;k++)
            {
                v[k] = primitives.get(i+k);
            }

            //the newell's method to calculate normal

            for (k=0;k<primitiveSize;k++)
            {
                norm.x += (positions.get(v[k]).y-positions.get(v[(k+1)%primitiveSize]).y)*(positions.get(v[k]).z+positions.get(v[(k+1)%primitiveSize]).z);
                norm.y += (positions.get(v[k]).z-positions.get(v[(k+1)%primitiveSize]).z)*(positions.get(v[k]).x+positions.get(v[(k+1)%primitiveSize]).x);
                norm.z += (positions.get(v[k]).x-positions.get(v[(k+1)%primitiveSize]).x)*(positions.get(v[k]).y+positions.get(v[(k+1)%primitiveSize]).y);
            }
            norm = norm.normalize();


            for (k=0;k<primitiveSize;k++)
            {
                normals.set(v[k],normals.get(v[k]).add(norm));
            }
        }

        for (i=0;i<normals.size();i++)
        {
            normals.set(i,normals.get(i).normalize());
        }
    }

    /**
     * Add the transformed mesh "obj" using transform to this mesh and return this mesh.
     * This mesh remains unchanged
     * @param obj
     * @param transform
     * @return
     */
    public PolygonMesh add(PolygonMesh obj,Matrix4f transform)
    {

        /* trying out obj exporting by merging all models into one */
        int offset = this.getVertexCount();
        List<Vector4f> rpositions,rnormals,rtexcoords;
        List<Integer> rprimitives;
        rpositions = this.getVertexPositionsAsList();
        rnormals = this.getVertexNormalsAsList();
        rtexcoords = this.getTexCoordsAsList();
        rprimitives = this.getPrimitivesAsList();

        List<Vector4f> pos = obj.getVertexPositionsAsList();
        List<Vector4f> norm = obj.getVertexNormalsAsList();
        List<Vector4f> tex = obj.getTexCoordsAsList();
        List<Integer> prim = obj.getPrimitivesAsList();

        Matrix4f invTranspose = new Matrix4f(transform);
        invTranspose = invTranspose.invert().transpose();

        for (int j=0;j<pos.size();j++)
        {
            Vector4f p = pos.get(j);
            p = transform.transform(p);
            rpositions.add(p);
        }
        for (int j=0;j<norm.size();j++)
        {
            Vector4f p = norm.get(j);
            p = invTranspose.transform(p);
            rnormals.add(p);
        }

        for (int j=0;j<tex.size();j++)
        {
            Vector4f p = tex.get(j);
            rtexcoords.add(p);
        }

        for (int j=0;j<prim.size();j++)
        {
            rprimitives.add(offset + prim.get(j));
        }
        offset += pos.size();

        //now export
        util.PolygonMesh result = new util.PolygonMesh();
        result.setVertexPositions(rpositions);
        result.setNormals(rnormals);
        result.setPrimitives(rprimitives);
        result.setPrimitiveSize(3);

        return result;
    }
}
