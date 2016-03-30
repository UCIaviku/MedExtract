public class Token {
	
	public static enum Kind {
		AND("and"),
		
		REGEX("regex"),

		DICTIONARY("dictionary"),
		
		FROM("from"),
		FILE("file"),
		
		SEMICOLON(";"),
		
		IDENTIFIER(),
		LITERAL(),
		ERROR(),
		EOF();
		
		private String default_lexeme;
		
		Kind()
		{
			default_lexeme = "";
		}
		
		Kind(String lexeme)
		{
			default_lexeme = lexeme;
		}
		
		public boolean hasStaticLexeme()
		{
			return default_lexeme != null;
		}
		
		// OPTIONAL: if you wish to also make convenience functions, feel free
		//           for example, boolean matches(String lexeme)
		//           can report whether a Token.Kind has the given lexeme
	}
	
	private int lineNum;
	private int charPos;
	Kind kind;
	private String lexeme = "";
	
	
	// OPTIONAL: implement factory functions for some tokens, as you see fit
	         
	public static Token EOF(int lineNum, int charPos)
	{
		Token tok = new Token(lineNum, charPos);
		tok.kind = Kind.EOF;
		return tok;
	}
	
	public static Token LITERAL(String lexeme, int lineNum, int charPos)
	{
		Token tok = new Token(lineNum, charPos);
		tok.kind = Kind.LITERAL;
		tok.lexeme = lexeme;
		return tok;
	}
	
	public static Token IDENTIFIER(String lexeme, int lineNum, int charPos)
	{
		Token tok = new Token(lineNum, charPos);
		tok.kind = Kind.IDENTIFIER;
		tok.lexeme = lexeme;
		return tok;
	}

	private Token(int lineNum, int charPos)
	{
		this.lineNum = lineNum;
		this.charPos = charPos;
		
		// if we don't match anything, signal error
		this.kind = Kind.ERROR;
		this.lexeme = "No Lexeme Given";
	}
	
	public Token(String lexeme, int lineNum, int charPos)
	{
		this.lineNum = lineNum;
		this.charPos = charPos;
		
		boolean tokenFound = false;
		
		// find corresponding token if there's a default one
		for (Kind k : Kind.values()) {
			if ((!k.default_lexeme.equals("")) && (k.default_lexeme.equals(lexeme))) {
				this.kind = k;
				this.lexeme = lexeme;
				tokenFound = true;
			}
		}
		
		if (! tokenFound) {
			boolean isIdentifier = true;
			if (! ((Character.isLetter(lexeme.charAt(0))) || lexeme.charAt(0) == '_')) {
				isIdentifier = false;
			}
			for (int i = 1; i < lexeme.length(); ++i) {
				if (! (isIdentifier = (Character.isLetterOrDigit(lexeme.charAt(i))) || lexeme.charAt(i) == '_')) {
					isIdentifier = false;
				}
			}
			if (isIdentifier) {
				tokenFound = true;
				this.kind = Kind.IDENTIFIER;
				this.lexeme = lexeme;
			}
		}
				
		// if we don't match anything, signal error
		if (! tokenFound) {
			this.kind = Kind.ERROR;
			this.lexeme = lexeme;
		}

	}
	
	public int lineNumber()
	{
		return lineNum;
	}
	
	public int charPosition()
	{
		return charPos;
	}
	
	// Return the lexeme representing or held by this token
	public String lexeme()
	{
		return this.lexeme;
	}
	
	public String toString()
	{
		String s;
		if (this.kind == Kind.EOF) {
			s = String.format("EOF(lineNum:%d, charPos:%d)", this.lineNum, this.charPos);
		} else if (this.kind == Kind.ERROR) {
			s = String.format("ERROR(Unexpected character: %s)(lineNum:%d, charPos:%d)", this.lexeme, this.lineNum, this.charPos);
		} else {
			s = String.format("%s(lineNum:%d, charPos:%d)", this.kind, this.lineNum, this.charPos);
		}
		return s;
	}

	public boolean is(Kind expectedKind)
	{
		return this.kind == expectedKind;
	}

	public Token.Kind kind()
	{
		return this.kind;
	}
	
	// OPTIONAL: function to query a token about its kind
	//           boolean is(Token.Kind kind)
	
	// OPTIONAL: add any additional helper or convenience methods
	//           that you find make for a clean design

}
