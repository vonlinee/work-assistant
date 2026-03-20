package org.assistant.editor;

public enum Language {

	JAVA("java"),
	PYTHON("python"),
	JAVASCRIPT("javascript"),
	PHP("php"),
	C("c"),
	CPP("cpp"),
	C_SHARP("csharp"),
	RUBY("ruby"),
	GO("go"),
	KOTLIN("kotlin"),
	SCALA("scala"),
	DART("dart"),
	LUA("lua"),
	;

	private final String name;

	Language(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
