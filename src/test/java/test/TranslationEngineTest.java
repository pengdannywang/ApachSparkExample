package test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class TranslationEngineTest {

	private TranslatorEngine translatorEngine=new TranslatorEngine();


	@TestFactory
	public Stream<DynamicTest> translateDynamicTestsFromStream() {

		List<String> inPhrases =
				new ArrayList<>(Arrays.asList("Hello", "Yes", "No", "Goodbye", "Good night", "Thank you"));
		List<String> outPhrases =
				new ArrayList<>(Arrays.asList("Bonjour", "Oui", "Non", "Au revoir", "Bonne nuit", "Merci"));

		return inPhrases.stream().map(phrs -> DynamicTest.dynamicTest("Test translate " + phrs, () -> {
			int idx = inPhrases.indexOf(phrs);
			System.out.println(outPhrases.get(idx)+","+ this.translatorEngine.tranlate(phrs));
			assertEquals(outPhrases.get(idx), this.translatorEngine.tranlate(phrs));
		}));
	}

}