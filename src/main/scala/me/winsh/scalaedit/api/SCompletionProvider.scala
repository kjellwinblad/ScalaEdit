package me.winsh.scalaedit.api
import org.fife.ui.autocomplete._


object SCompletionProvider {

	val keywordJava= List("abstract","assert","break","case","catch","class","const","continue","default","do","else","enum","extends",
	"final","finally","for","goto","if","implements","import","instanceof","interface","native","new","package","private","protected",
	"public","return","static","strictfp","super","switch","synchronized","this","throw","throws","transient","try","void","volatile","while")  

	val keywordScala=List("abstract","boolean","byte","case","catch","char","class","def","do","double","else","extends","false","final",
	"finally","float","for","forSome","if","implicit","import","int","lazy","long","match","new","null","object","override","package",
	"private","protected","requires","return","sealed","short","super","this","throw","trait","true","try","type","val","var","while","with","yield")
	
	def  createCompeletionProvider( keywords:List[String]):org.fife.ui.autocomplete.CompletionProvider={ 
      
       var  provider = new DefaultCompletionProvider


    // Add completions for all Java keywords.  A BasicCompletion is just
      // a straightforward word completion.


		for(keyword <- keywords)  provider.addCompletion(new BasicCompletion(provider, keyword))
		
   
      // Add a couple of "shorthand" completions.  These completions don't
      // require the input text to be the same thing as the replacement text.
      provider.addCompletion(new ShorthandCompletion(provider, "sysout", "System.out.println("))
      provider.addCompletion(new ShorthandCompletion(provider, "syserr", "System.err.println("))
      provider.addCompletion(new ShorthandCompletion(provider, "cls", "Class(\n)"))

      provider


	}

}

   