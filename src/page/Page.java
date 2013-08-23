package page;

public class Page implements Cloneable {
	public static final  int PAGE_SIZE = 1024;
	
	private char[] contents;
	
	private Page() {
		this.contents = new char[PAGE_SIZE];
		for (int i = 0; i < PAGE_SIZE; i++) {
			this.contents[i] = '\0';
		}
	}
	
	public static Page makePage() {
		return new Page();
	}
	
	public Page getCopy() {
		Page copy = new Page();
		
		for (int i = 0; i < PAGE_SIZE; i++) {
			copy.contents[i] = this.contents[i];
		}
		
		return copy;
	}
}
