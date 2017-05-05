package heli.org.helidroid;

import android.opengl.GLES20;

import javax.microedition.khronos.opengles.GL10;

/**
 * helper class for Apachi Open GL transformations
 * @author abelouso
 *
 */
public class ApachiGL
{

    /**
     * ::toFA convert point 3D into float array
     * @param p
     * @return
     */
    public float[] toFA(Point3D p)
    {
        float res[] = new float[]
                {
                        (float)(p.x()),
                        (float)(p.y()),
                        (float)(p.z())
                };
        return res;
    }

    /**
     * ::toFA converts pint 3d into float array with for elements
     * @param p
     * @param nx
     * @return
     */
    public float[] toFA(Point3D p, float nx)
    {
        float res[] = new float[]
                {
                        (float)(p.x()),
                        (float)(p.y()),
                        (float)(p.z()),
                        nx
                };
        return res;
    }
    /**
     * ::setEmission sets material Emission
     * @param gl
     * @param col
     */
    public void setEmission(GL10 gl, Point3D col)
    {
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_EMISSION, toFA(col,0), 0);
    }

    /**
     * ::setMaterial sets material
     * @param gl
     * @param color
     * @param spec
     * @param alpha
     * @param shine
     * @param amb
     */
    public void setMaterial(GL10 gl, Point3D color, Point3D spec, float alpha, float shine, float amb)
    {
        float mat_shine[] = new float[] { shine };
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK
                ,GL10.GL_DIFFUSE, toFA(color,alpha), 0);
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK
                ,GL10.GL_AMBIENT, toFA(Point3D.mult(color,amb),alpha), 0);

        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, mat_shine, 0);
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, toFA(spec,alpha), 0);
    }

    /**
     * ::box - draws box in open gl
     * @param gl
     * @param alpha: transparency
     * @param wh: where
     * @param scl: n/a
     * @param size: size of the box
     */
    /*
    public void box(GL10 gl, float alpha, Point3D wh, Point3D scl, double size)
    {
        Point3D w = wh;//to3D(wh,scl);
        size = size * 0.5;
        gl.glBegin(GL2.GL_POLYGON);// f1: front
        gl.glNormal3f(-1.0f,0.0f,0.0f);
        gl.glVertex3d(w.m_x - size,w.m_y - size,w.m_z - size);
        gl.glVertex3d(w.m_x - size,w.m_y - size,w.m_z + size);
        gl.glVertex3d(w.m_x + size,w.m_y - size,w.m_z + size);
        gl.glVertex3d(w.m_x + size,w.m_y - size,w.m_z - size);
        gl.glEnd();
        gl.glBegin(GL2.GL_POLYGON);// f2: bottom

        gl.glNormal3f(0.0f,0.0f,-1.0f);
        gl.glVertex3d(w.m_x - size,w.m_y - size,w.m_z - size);
        gl.glVertex3d(w.m_x + size,w.m_y - size,w.m_z - size);
        gl.glVertex3d(w.m_x + size,w.m_y + size,w.m_z - size);
        gl.glVertex3d(w.m_x - size,w.m_y + size,w.m_z - size);
        gl.glEnd();
        gl.glBegin(GL2.GL_POLYGON);// f3:back
        gl.glNormal3f(1.0f,0.0f,0.0f);
        gl.glVertex3d(w.m_x + size,w.m_y + size,w.m_z - size);
        gl.glVertex3d(w.m_x + size,w.m_y + size,w.m_z + size);
        gl.glVertex3d(w.m_x - size,w.m_y + size,w.m_z + size);
        gl.glVertex3d(w.m_x - size,w.m_y + size,w.m_z - size);
        gl.glEnd();
        gl.glBegin(GL2.GL_POLYGON);// f4: top
        gl.glNormal3f(0.0f,0.0f,1.0f);
        gl.glVertex3d(w.m_x + size,w.m_y + size,w.m_z + size);
        gl.glVertex3d(w.m_x + size,w.m_y - size,w.m_z + size);
        gl.glVertex3d(w.m_x - size,w.m_y - size,w.m_z + size);
        gl.glVertex3d(w.m_x - size,w.m_y + size,w.m_z + size);
        gl.glEnd();
        gl.glBegin(GL2.GL_POLYGON);// f5: left
        gl.glNormal3f(0.0f,1.0f,0.0f);
        gl.glTexCoord2d(0, 0);
        gl.glVertex3d(w.m_x - size,w.m_y - size,w.m_z - size);
        gl.glVertex3d(w.m_x - size,w.m_y + size,w.m_z - size);
        gl.glVertex3d(w.m_x - size,w.m_y + size,w.m_z + size);
        gl.glVertex3d(w.m_x - size,w.m_y - size,w.m_z + size);
        gl.glEnd();
        gl.glBegin(GL2.GL_POLYGON);// f6: right
        gl.glNormal3f(0.0f,-1.0f,0.0f);
        gl.glTexCoord2d(0, 0);
        gl.glVertex3d(w.m_x + size,w.m_y - size,w.m_z - size);
        gl.glVertex3d(w.m_x + size,w.m_y - size,w.m_z + size);
        gl.glVertex3d(w.m_x + size,w.m_y + size,w.m_z + size);
        gl.glVertex3d(w.m_x + size,w.m_y + size,w.m_z - size);
        gl.glEnd();
    } */

}
