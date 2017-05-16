package heli.org.helidroid;

import java.nio.*;

public class BufferUtils
{
	public static ByteBuffer getBB(byte[] src)
    {
        ByteBuffer idxs = ByteBuffer.allocateDirect(src.length);
        idxs.order(ByteOrder.nativeOrder());
        idxs.put(src);
        idxs.position(0);
        return idxs;
    }

    public static FloatBuffer getFB(float[] src)
    {
        ByteBuffer idxs = ByteBuffer.allocateDirect(src.length * 4);
        idxs.order(ByteOrder.nativeOrder());
        FloatBuffer fb = idxs.asFloatBuffer();
        fb.put(src);
        fb.position(0);
        return fb;
    }

    public static IntBuffer getIB(int[] src)
    {
        ByteBuffer idxs = ByteBuffer.allocateDirect(src.length * 4);
        idxs.order(ByteOrder.nativeOrder());
        IntBuffer sb = idxs.asIntBuffer();
        sb.put(src);
        sb.position(0);
        return sb;
    }

	
}
