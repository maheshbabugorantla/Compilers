##STEP 3 CODE

<p> Here my goal is to process variable declarations and create Symbol Tables. A symbol table is a data structure that keeps information about non-keyword symbols that appear in source programs.</p><p> <bold><em>Variables</em></bold> and <bold><em>String Variables</em></bold> are examples of such symbols. Other example of symbols kept by the symbol table are the names of functions or procedures. The Symbols added to the symbol table will be sued in many of the further phases of the compilation.</p>

<p>In this step of project, I have implemented DataStructures to constrcut symbol tables for each scope in the program.</p>


### Handling Errors
<p>The compiler will output the string "DECLARATION ERROR <var_name>" if there are two declarations with the same name in the same scope</p>

### Output Format
<p> For each symbol table in the program, I used the following format to print the Symbol Tables (Even Nested Symbol Tables):</p>
<br><p>Symbol table &lt;scope_name&gt;</p>
<br><p>name &lt;var_name&gt; type &lt;type_name&gt;</p>
<br><p>name &lt;var_name&gt; type &lt;type_name&gt; value &lt;string_value&gt;</p>
<br><p>...</p>

### GuideLines from the ECE 468 Instructor

Please put your antlr.jar file in "lib" directory 
and submit it with other files.

The provided Makefile is for ANTLR v4,
Please edit your own Makefile if you're using v3 or something else.

Edit the Makefile with actual group member names.

Micro.g4 is the grammar file that you should edit and Micro.java will be your main java program after you edit it. 
