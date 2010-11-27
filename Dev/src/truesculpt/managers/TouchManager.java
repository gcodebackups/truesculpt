package truesculpt.managers;

import truesculpt.managers.ToolsManager.EPovToolSubMode;
import truesculpt.managers.ToolsManager.EToolMode;
import truesculpt.utils.Global;
import truesculpt.utils.Utils;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

//To detect sculpture action, zoom and pan actions based on gesture
public class TouchManager extends BaseManager {

	public TouchManager(Context baseContext) {
		super(baseContext);
		// TODO Auto-generated constructor stub
	}

	private float mLastX=0.0f;
	private float mLastY=0.0f;
	private float mRotInit=0.0f;
	private float mElevInit=0.0f;
	private float mZoomInit=0.0f;
	private float mLastFingerSpacing=0.0f;
	private long mLastTapTapTime=0;
	
	private float fDemultRotateFactor=2.5f;	
	private float fDemultZoomFactor=fDemultRotateFactor/20.0f;
	private float fTapTapThresold=1000.0f;//ms
		
	//ScaleGestureDetector mScaleGestureDetector = new ScaleGestureDetector();
	public void onTouchEvent(MotionEvent event)
	{
		//String msg="Pressure = " + Float.toString(event.getPressure());
		//String msg="(x,y) = (" + Float.toString(event.getX()) +"," +Float.toString(event.getY()) + ")";
		//Log.i(Global.TAG,msg);
		
		//dumpEvent(event);
		
		float x=event.getX();
		float y=event.getY();	
		
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;

		switch(actionCode)		
		{
			case MotionEvent.ACTION_DOWN:
			{				
				long curTapTapTime=System.currentTimeMillis();
				if ((curTapTapTime-mLastTapTapTime) < fTapTapThresold)
				{
					StartTapTapAction();
				}
				mLastTapTapTime=curTapTapTime;
				
				initPOVValues(x, y);
				getManagers().getToolsManager().setPovSubMode(EPovToolSubMode.ROTATE);
				
				// auto switch tool mode
				int nRes=getManagers().getMeshManager().Pick(x, y);
				if (nRes<0)
				{
					EToolMode currMode = getManagers().getToolsManager().getToolMode();
					if (currMode!=EToolMode.POV)
					{						
						if (getManagers().getToolsManager().getForcedMode()==false)
						{
							getManagers().getToolsManager().setToolMode(EToolMode.POV);
						}											
					}
				}
				else
				{
					EToolMode currMode = getManagers().getToolsManager().getToolMode();
					if (currMode==EToolMode.POV)
					{
						if (getManagers().getToolsManager().getForcedMode()==false)
						{
							getManagers().getToolsManager().setLastToolMode();	
						}						
					}
				}
				
				getManagers().getToolsManager().setForcedMode(false);
				
				break;
			}
					
			case MotionEvent.ACTION_POINTER_DOWN:
			{				
				initPOVValues(x, y);//reinit rotate values
				
				mZoomInit=getManagers().getPointOfViewManager().getZoomDistance();	
				mLastFingerSpacing=getDistanceBetweenFingers(event);;
				
				getManagers().getToolsManager().setPovSubMode(EPovToolSubMode.ZOOM);				
				break;
			}
			case MotionEvent.ACTION_POINTER_UP:
			{
				initPOVValues(x, y);//reinit rotate values
				
				getManagers().getToolsManager().setPovSubMode(EPovToolSubMode.ROTATE);				
				break;
			}
			case MotionEvent.ACTION_UP:
			{
				initPOVValues(x, y);//reinit rotate values
				
				getManagers().getToolsManager().setPovSubMode(EPovToolSubMode.ROTATE);				
				break;
			}
			case MotionEvent.ACTION_MOVE:
			{
				switch (getManagers().getToolsManager().getToolMode())
				{
					case POV:
					{		
						if (getManagers().getToolsManager().getPovSubMode()==EPovToolSubMode.ROTATE)
						{
							float angleRot =mRotInit + (x-mLastX)/fDemultRotateFactor;
							float angleElev= mElevInit + (y-mLastY)/fDemultRotateFactor;										
							float dist =getManagers().getPointOfViewManager().getZoomDistance();
							
							getManagers().getPointOfViewManager().SetAllAngles(angleRot,angleElev,dist);
						}
						if (getManagers().getToolsManager().getPovSubMode()==EPovToolSubMode.ZOOM)
						{
							float fingersSpacing=getDistanceBetweenFingers(event);
							float dist =mZoomInit + (fingersSpacing-mLastFingerSpacing)/fDemultZoomFactor;
							
							getManagers().getPointOfViewManager().setZoomDistance(dist);						
						}
						
						break;
					}
				
					case SCULPT:
					case PAINT:
					{				
						getManagers().getMeshManager().Pick(x, y);
						break;										
					}				
				}
				
				break;
			}
		}
	}
	
	private void StartTapTapAction()
	{
		switch (getManagers().getToolsManager().getToolMode())
		{
			case POV:
			{		
				//TODO queue or post to come back in UI thread 
				Utils.StartMyActivity(getbaseContext(), truesculpt.ui.panels.PointOfViewPanel.class);				
				break;
			}
			case SCULPT:
			case PAINT:
			{	
				//TODO queue or post to come back in UI thread
				Utils.StartMyActivity(getbaseContext(),truesculpt.ui.panels.OptionsPanel.class);
				break;
			}
		}
	}
	
	/** Show an event in the LogCat view, for debugging */
	private void dumpEvent(MotionEvent event) {
	   String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
	      "POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
	   StringBuilder sb = new StringBuilder();
	   int action = event.getAction();
	   int actionCode = action & MotionEvent.ACTION_MASK;
	   sb.append("event ACTION_" ).append(names[actionCode]);
	   if (actionCode == MotionEvent.ACTION_POINTER_DOWN
	         || actionCode == MotionEvent.ACTION_POINTER_UP) {
	      sb.append("(pid " ).append(
	      action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
	      sb.append(")" );
	   }
	   sb.append("[" );
	   for (int i = 0; i < event.getPointerCount(); i++) {
	      sb.append("#" ).append(i);
	      sb.append("(pid " ).append(event.getPointerId(i));
	      sb.append(")=" ).append((int) event.getX(i));
	      sb.append("," ).append((int) event.getY(i));
	      if (i + 1 < event.getPointerCount())
	         sb.append(";" );
	   }
	   sb.append("]" );
	   Log.d("POINTER", sb.toString());
	}
	//TODO handle correct touched finger
	private void initPOVValues(float x, float y) {		
		mLastX=x;
		mLastY=y;		
		mRotInit=getManagers().getPointOfViewManager().getRotationAngle();
		mElevInit=getManagers().getPointOfViewManager().getElevationAngle();			
	}
	
	private float getDistanceBetweenFingers(MotionEvent event) {
		   float x = event.getX(0) - event.getX(1);
		   float y = event.getY(0) - event.getY(1);
		   return (float) Math.sqrt(x * x + y * y);
		}

	@Override
	public void onCreate() {		
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
	}
}
