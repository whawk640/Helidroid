package heli.org.helidroid;

public class ApachiControl extends Thread
{ public static final String TAG = "ApachiControl";
  public static final long DBG = 0x100;
  public static final double CHANGE_INC = 20.0;
  public static final double HOLD_INC = 10.0;
  public static final double INIT_SPEED = 10.0;
  public static final int RT_SLEEP = 200;
  protected World m_world;
  protected Apachi m_chopper;
  
  protected int m_tick_ms = 200;
  
  public ApachiControl(Apachi chop, World world)
  {
    m_world = world;
    m_chopper = chop;
  }
  
  public void fly()
  {
    Point3D pos = null;
    synchronized (m_world)
    {
      pos = m_world.gps(m_chopper.getId());
    }
    boolean landed = m_world.isAirborn(m_chopper.getId()) == 0;
    int slow = 70;
    double wts = m_world.getTimestamp();
    
    if(landed && wts > (slow + 200))
    {
      m_chopper.shutdown();
      return;
    }
    
    if(wts > 30 && wts < slow)
    {
      m_chopper.maintainAlt(90);
      m_chopper.maintainSpeed(5.0,-1.0);
    }

    if(wts > slow && m_chopper.getCurrentSpeed() > 0.05)
    {
      m_chopper.maintainSpeed(0.0,-1.0);
    }

    if(wts > slow && m_chopper.getCurrentSpeed() < 0.049)
    {
      m_chopper.hover(0.0);
    }
    
  }
  
  @Override
  public void run()
  {
    while(true)
    {
      fly();
      try { Thread.sleep(m_tick_ms); } catch(Exception e) {}
    }
  }
}
