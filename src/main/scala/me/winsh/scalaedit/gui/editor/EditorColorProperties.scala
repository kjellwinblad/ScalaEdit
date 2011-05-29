/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.gui

import me.winsh.scalaedit.api._
import java.io.File
import me.winsh.scalaedit.gui._
import java.awt.Color

class EditorColorProperties
  extends PropertiesFile(new File(Utils.propertiesDir, "editor.properties"),
    """Editor Color Properties""") {
/*
 List(
 comment_documentation,
 comment_eol,
 comment_multiline,
 data_type,
 error_char,
 error_identifier,
 error_number_format,
 error_string_double,
 function,
 identifier,
 literal_backquote,
 literal_boolean,
 literal_char,
 literal_number_decimal_int,
 literal_number_float,
 literal_number_hexadecimal,
 literal_string_double_quote,
 markup_tag_attribute,
 markup_tag_delimiter,
 markup_tag_name,
 null,
 num_token_types,
 operator,
 preprocessor,
 reserved_word,
 separator,
 variable,
 whitespace)

val  textArea = new RSyntaxTextArea(20, 60);

val scheme = textArea.getSyntaxScheme();

def create(sl:List[String]){
	
}
*/

}