package truesculpt.managers;

import java.util.Vector;

import android.content.Context;

public class PointOfViewManager extends BaseManager {

	public interface OnPointOfViewChangeListener
	{
		void onPointOfViewChange();
	}
	private Vector<OnPointOfViewChangeListener> mListeners= new Vector<OnPointOfViewChangeListener>();
	//camera pos
	private float mX=0.0f;
	
	//looked at point
	private float mXOrig=0.0f;
	private float mY=0.0f;
	private float mYOrig=0.0f;
	
	private float mZ=10.0f;
	
	private float mZOrig=0.0f;
	
	public PointOfViewManager(Context baseContext) {
		super(baseContext);
		// TODO Auto-generated constructor stub
	}
	
	public void addElevationAngle(float angle)
	{
		NotifyListeners();
	}
	
	public void addRotationAngle(float angle)
	{
		NotifyListeners();
	}	
	public void addZoomDistance(float dist)
	{
		NotifyListeners();
	}
	
	private void NotifyListeners()
	{
		for (OnPointOfViewChangeListener listener : mListeners) 
		{
			listener.onPointOfViewChange();		
		}	
	}
	
	public void registerPointOfViewChangeListener(OnPointOfViewChangeListener listener)
	{
		mListeners.add(listener);	
	}	


}
