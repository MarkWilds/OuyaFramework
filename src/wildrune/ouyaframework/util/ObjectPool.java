package wildrune.ouyaframework.util;

import java.util.Stack;

/**
 * Pool class handling pooling
 * @author Wildrune
 */
public class ObjectPool<T> implements IPool<T>
{
	private final Class<T> mReference;
	private final Stack<T> mFreeObjects;
	
	/**
	 * Default constructor
	 */
	public ObjectPool(Class<T> ref)
	{
		mReference = ref;
		mFreeObjects = new Stack<T>();
	}

	/**
	 * Recycles the object
	 */
	@Override
	public void Recycle(T data) 
	{
		mFreeObjects.push(data);
	}

	@Override
	public T Get() 
	{
		if(mFreeObjects.isEmpty())
		{
			try 
			{
				return mReference.newInstance();
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			
			return null;
		}
		
		return mFreeObjects.pop();
	}

	@Override
	public void Reset() 
	{
		mFreeObjects.clear();
	}

	@Override
	public String Debug() 
	{
		return "Pool size: " + mFreeObjects.size();
	}

}
