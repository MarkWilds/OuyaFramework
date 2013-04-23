package wildrune.ouyaframework.utils.interfaces;

/**
 * Pool interfac
 * @author Wildrune
 */
public interface IPool<T>
{
	public void Recycle(T data);
	public T Get();	
	public void Reset();
	public String Debug();
}
