package util;

import org.joml.Vector4f;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ashesh on 2/1/2016.
 */
public class ObjExporter
{
    public static boolean exportFile(PolygonMesh mesh,OutputStream out)
    {
        PrintWriter printer = new PrintWriter(out);
        int i,j;

        List<Vector4f> vertices = mesh.getVertexPositionsAsList();
        List<Vector4f> normals = mesh.getVertexNormalsAsList();
        List<Vector4f> texcoords = mesh.getTexCoordsAsList();
        List<Integer> primitives = mesh.getPrimitivesAsList();

        for (i=0;i<vertices.size();i++)
        {
            printer.println("v " + vertices.get(i).x + " " + vertices.get(i).y + " " + vertices.get(i).z);
        }

        for (i=0;i<normals.size();i++)
        {
            printer.println("vn " + normals.get(i).x + " " + normals.get(i).y + " " + normals.get(i).z);
        }

        for (i=0;i<texcoords.size();i++)
        {
            printer.println("vt " + texcoords.get(i).x + " " + texcoords.get(i).y + " " + texcoords.get(i).z);
        }


        //polygons

        for (i=0;i<primitives.size();i+=mesh.getPrimitiveSize())
        {
            printer.print("f ");
            for (j=0;j<mesh.getPrimitiveSize();j++)
            {
                printer.print(primitives.get(i+j)+1 + " ");
            }
            printer.println();
        }
        printer.close();
        return true;
    }
}
