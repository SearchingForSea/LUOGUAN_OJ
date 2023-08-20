import java.util.ArrayList;
import java.util.List;

// 无限占用空间
public class Main {
	public static void main(String[] args) {
		List<byte[]> bytes = new ArrayList<byte[]>();
		while (true) {
			bytes.add(new byte[10000]);
		}
	}
}