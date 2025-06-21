package com.akuleshov7.ktoml.parsers

import com.akuleshov7.ktoml.Toml.Default.tomlParser
import com.akuleshov7.ktoml.tree.nodes.TomlArrayOfTablesElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArraysOfTablesTest {
    @Test
    fun positiveSimpleParsing() {
        val string = """
            [[fruits]]
            a = "apple"
            b = "qwerty"

            [[fruits]]
            a = "banana"
            b = "qwerty"

            [[fruits]]
            a = "plantain"
            b = "qwerty"
        """.trimIndent()

        val parsedToml = tomlParser.parseString(string)
        parsedToml.prettyPrint()
        assertEquals(
            """
                | - TomlFile (rootNode)
                |     - TomlTable ([[fruits]])
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlKeyValuePrimitive (a="apple")
                |             - TomlKeyValuePrimitive (b="qwerty")
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlKeyValuePrimitive (a="banana")
                |             - TomlKeyValuePrimitive (b="qwerty")
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlKeyValuePrimitive (a="plantain")
                |             - TomlKeyValuePrimitive (b="qwerty")
                |
        """.trimMargin(),
            parsedToml.prettyStr()
        )
    }

    @Test
    fun parsingOfEmptyArrayOfTables() {
        val string = """
            [[products]]
                name = "Hammer"
                sku = 738594937
        
            [[products]]  
        
            [[products]]
                name = "Nail"
                sku = 284758393
            
                color = "gray"
        """.trimIndent()

        val parsedToml = tomlParser.parseString(string)
        parsedToml.prettyPrint()

        assertEquals(
            """
                | - TomlFile (rootNode)
                |     - TomlTable ([[products]])
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlKeyValuePrimitive (name="Hammer")
                |             - TomlKeyValuePrimitive (sku=738594937)
                |         - TomlArrayOfTablesElement (technical_node)
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlKeyValuePrimitive (name="Nail")
                |             - TomlKeyValuePrimitive (sku=284758393)
                |             - TomlKeyValuePrimitive (color="gray")
                |
        """.trimMargin(),
            parsedToml.prettyStr()
        )
    }

    @Test
    fun parsingNestedArraysOfTables1() {
        val string = """
            [[fruits.varieties]] 
                name = "red delicious"
            
            [[fruits.varieties.inside]]
                name = "granny smith"
        """.trimIndent()

        val parsedToml = tomlParser.parseString(string)
        parsedToml.prettyPrint()

        assertEquals(
            """
                | - TomlFile (rootNode)
                |     - TomlTable ([[fruits]])
                |         - TomlTable ([[fruits.varieties]])
                |             - TomlArrayOfTablesElement (technical_node)
                |                 - TomlKeyValuePrimitive (name="red delicious")
                |                 - TomlTable ([[fruits.varieties.inside]])
                |                     - TomlArrayOfTablesElement (technical_node)
                |                         - TomlKeyValuePrimitive (name="granny smith")
                |
        """.trimMargin(),
            parsedToml.prettyStr()
        )
    }

    @Test
    fun parsingNestedArraysOfTables2() {
        val string = """
            [[fruits.varieties]] 
                name = "red delicious"
            
            [fruits.varieties.inside]
                name = "granny smith"
        """.trimIndent()

        val parsedToml = tomlParser.parseString(string)
        parsedToml.prettyPrint()

        assertEquals(
            """
                | - TomlFile (rootNode)
                |     - TomlTable ([[fruits]])
                |         - TomlTable ([[fruits.varieties]])
                |             - TomlArrayOfTablesElement (technical_node)
                |                 - TomlKeyValuePrimitive (name="red delicious")
                |                 - TomlTable ([fruits.varieties.inside])
                |                     - TomlKeyValuePrimitive (name="granny smith")
                |
        """.trimMargin(),
            parsedToml.prettyStr()
        )
    }

    @Test
    fun parsingSimpleArrayOfTables1() {
        val string = """
            [[fruits.varieties]] 
                name = "red delicious"
            
            [[fruits.varieties]]
                name = "granny smith"
            
            [[fruits.varieties]]
                name = "granny smith"
        """.trimIndent()

        val parsedToml = tomlParser.parseString(string)
        parsedToml.prettyPrint()

        val fruitVarieties = parsedToml.children.last().children.last()
        assertEquals(3, fruitVarieties.children.size)
        fruitVarieties.children.forEach {
            assertTrue { it is TomlArrayOfTablesElement }
        }
    }

    @Test
    fun parsingSimpleArrayOfTables2() {
        //    FixMe: here should throw an exception in case of table duplication https://github.com/akuleshov7/ktoml/issues/30
        val string = """
            [[fruits.varieties]] 
                name = "red delicious"
            
            [fruits.varieties]
                name = "granny smith"
                
            [a]
                b = 1
            
            [[a]]
                b = 1
        """.trimIndent()

        val parsedToml = tomlParser.parseString(string)
        parsedToml.prettyPrint()
    }

    @Test
    fun parsingSimpleArrayOfTables3() {
        val string = """
            [[a]] 
            [[b]]
            [[a.b]]
            [[a.b]]
        """.trimIndent()

        val parsedToml = tomlParser.parseString(string)
        parsedToml.prettyPrint()
        assertEquals(
            """ 
                | - TomlFile (rootNode)
                |     - TomlTable ([[a]])
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlTable ([[a.b]])
                |                 - TomlArrayOfTablesElement (technical_node)
                |                 - TomlArrayOfTablesElement (technical_node)
                |     - TomlTable ([[b]])
                |         - TomlArrayOfTablesElement (technical_node)
                |
            """.trimMargin(),
            parsedToml.prettyStr()
        )

    }

    @Test
    fun parsingSimpleArrayOfTables4() {
        val string = """
            [[a]] 
            [[b]]
            [[a.b]]
            [[a]]
            [[a.b]]
        """.trimIndent()

        val parsedToml = tomlParser.parseString(string)
        parsedToml.prettyPrint()
        assertEquals(
            """ 
                | - TomlFile (rootNode)
                |     - TomlTable ([[a]])
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlTable ([[a.b]])
                |                 - TomlArrayOfTablesElement (technical_node)
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlTable ([[a.b]])
                |                 - TomlArrayOfTablesElement (technical_node)
                |     - TomlTable ([[b]])
                |         - TomlArrayOfTablesElement (technical_node)
                |
                """.trimMargin(),
            parsedToml.prettyStr()
        )
    }

    @Test
    fun tomlExampleParsingTest() {
        val string = """
            [[fruits]]
                name = "apple"
            
            # this is processed incorrectly now
            [fruits.physical]
                color = "red"
                shape = "round"
            
            # this is processed incorrectly now
            [fruits.physical.inside]
                color = "red"
                shape = "round"
               
            # this is processed incorrectly now
            [vegetables]
                outSideOfArray = true
            
            [[fruits.varieties]] 
                name = "red delicious"
            
            [[fruits.varieties]]
                name = "granny smith"
            
            [[fruits]]
                name = "banana"
            
            [[fruits.varieties]]
                name = "plantain"
        """.trimIndent()

        val parsedToml = tomlParser.parseString(string)
        parsedToml.prettyPrint()

        assertEquals(
            """ 
                    | - TomlFile (rootNode)
                    |     - TomlTable ([[fruits]])
                    |         - TomlArrayOfTablesElement (technical_node)
                    |             - TomlKeyValuePrimitive (name="apple")
                    |             - TomlTable ([fruits.physical])
                    |                 - TomlKeyValuePrimitive (color="red")
                    |                 - TomlKeyValuePrimitive (shape="round")
                    |             - TomlTable ([fruits.physical])
                    |                 - TomlTable ([fruits.physical.inside])
                    |                     - TomlKeyValuePrimitive (color="red")
                    |                     - TomlKeyValuePrimitive (shape="round")
                    |             - TomlTable ([[fruits.varieties]])
                    |                 - TomlArrayOfTablesElement (technical_node)
                    |                     - TomlKeyValuePrimitive (name="red delicious")
                    |                 - TomlArrayOfTablesElement (technical_node)
                    |                     - TomlKeyValuePrimitive (name="granny smith")
                    |         - TomlArrayOfTablesElement (technical_node)
                    |             - TomlKeyValuePrimitive (name="banana")
                    |             - TomlTable ([[fruits.varieties]])
                    |                 - TomlArrayOfTablesElement (technical_node)
                    |                     - TomlKeyValuePrimitive (name="plantain")
                    |     - TomlTable ([vegetables])
                    |         - TomlKeyValuePrimitive (outSideOfArray=true)
                    |
            """.trimMargin(), parsedToml.prettyStr()
        )
    }

    @Test
    fun nestedArraysOnly1() {
        val string = """
            [[a]]
                name = 1
            
            [[a.b]]
                name = 2
            
            [[a]]
                name = 3
            
            [[a.b]]
                name = 4
                
            [[c]]
                name = 5
        """.trimIndent()

        val parsedToml = tomlParser.parseString(string)
        parsedToml.prettyPrint()
        assertEquals(
            """
            | - TomlFile (rootNode)
            |     - TomlTable ([[a]])
            |         - TomlArrayOfTablesElement (technical_node)
            |             - TomlKeyValuePrimitive (name=1)
            |             - TomlTable ([[a.b]])
            |                 - TomlArrayOfTablesElement (technical_node)
            |                     - TomlKeyValuePrimitive (name=2)
            |         - TomlArrayOfTablesElement (technical_node)
            |             - TomlKeyValuePrimitive (name=3)
            |             - TomlTable ([[a.b]])
            |                 - TomlArrayOfTablesElement (technical_node)
            |                     - TomlKeyValuePrimitive (name=4)
            |     - TomlTable ([[c]])
            |         - TomlArrayOfTablesElement (technical_node)
            |             - TomlKeyValuePrimitive (name=5)
            |
        """.trimMargin(),
            parsedToml.prettyStr()
        )
    }

    @Test
    fun nestedArraysOnly2() {
        val string = """
            [[a]]
                name = 1
            
            [[a.b.c]]
                name = 2
            
            [[a]]
                name = 3
            
            [[a.b.c]]
                name = 4
                
            [[a.b.c.d]]
                
            [[c]]
                name = 5
        """.trimIndent()

        val parsedToml = tomlParser.parseString(string)
        parsedToml.prettyPrint()


    }

    @Test
    fun nestedArraysOnly3() {
        val string = """
           [[a.b]]
               [[a.b.c]]
               [[a.b.c]]
                  a = 1
                  [[a.b.c.d.e.f]]
           [[a.b]]
               # TABLE HERE, NOT ARRAY:
               [a.b.c]
        """.trimIndent()

        val parsedToml = tomlParser.parseString(string)
        parsedToml.prettyPrint()

        assertEquals(
            """
                        | - TomlFile (rootNode)
                        |     - TomlTable ([[a]])
                        |         - TomlTable ([[a.b]])
                        |             - TomlArrayOfTablesElement (technical_node)
                        |                 - TomlTable ([[a.b.c]])
                        |                     - TomlArrayOfTablesElement (technical_node)
                        |                     - TomlArrayOfTablesElement (technical_node)
                        |                         - TomlKeyValuePrimitive (a=1)
                        |                         - TomlTable ([[a.b.c.d]])
                        |                             - TomlTable ([[a.b.c.d.e]])
                        |                                 - TomlTable ([[a.b.c.d.e.f]])
                        |                                     - TomlArrayOfTablesElement (technical_node)
                        |             - TomlArrayOfTablesElement (technical_node)
                        |                 - TomlTable ([a.b.c])
                        |                     - TomlStubEmptyNode (technical_node)
                        |
        """.trimMargin(), parsedToml.prettyStr()
        )
    }

    @Test
    fun parsingNestedArraysOfTablesRegression() {
        val string = """
            [[a]]
                name = 1
            
            [a.b]
                name = 2
            
            [[a]]
                name = 3
            
            [a.b]
                name = 4
                
            [[c]]
                name = 5
        """.trimIndent()

        val parsedToml = tomlParser.parseString(string)
        parsedToml.prettyPrint()

        assertEquals(
            """
                | - TomlFile (rootNode)
                |     - TomlTable ([[a]])
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlKeyValuePrimitive (name=1)
                |             - TomlTable ([a.b])
                |                 - TomlKeyValuePrimitive (name=2)
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlKeyValuePrimitive (name=3)
                |             - TomlTable ([a.b])
                |                 - TomlKeyValuePrimitive (name=4)
                |     - TomlTable ([[c]])
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlKeyValuePrimitive (name=5)
                |
        """.trimMargin(),
            parsedToml.prettyStr()
        )
    }

    @Test
    fun testForStubTables() {
        val string = """
            [[a]]
              [a.b]
            [[a]]
              [a.b]
            [[a]]
              [a.b]
        """.trimIndent()

        val parsedToml = tomlParser.parseString(string)
        parsedToml.prettyPrint()

        assertEquals(
            """
                | - TomlFile (rootNode)
                |     - TomlTable ([[a]])
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlTable ([a.b])
                |                 - TomlStubEmptyNode (technical_node)
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlTable ([a.b])
                |                 - TomlStubEmptyNode (technical_node)
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlTable ([a.b])
                |                 - TomlStubEmptyNode (technical_node)
                |
        """.trimMargin(),
            parsedToml.prettyStr()
        )
    }

    @Test
    fun mixedTablesAndArrayOfTables1() {
        val string = """
            [[a.b]]
                name = 1
            
            [a.b.c]
                name = 2
            
            [[a.b]]
                name = 3
            
            [a.b.c]
                name = 4
                
            [[c]]
                name = 5
        """.trimIndent()

        val parsedToml = tomlParser.parseString(string)
        parsedToml.prettyPrint()
        assertEquals(
            """
                | - TomlFile (rootNode)
                |     - TomlTable ([[a]])
                |         - TomlTable ([[a.b]])
                |             - TomlArrayOfTablesElement (technical_node)
                |                 - TomlKeyValuePrimitive (name=1)
                |                 - TomlTable ([a.b.c])
                |                     - TomlKeyValuePrimitive (name=2)
                |             - TomlArrayOfTablesElement (technical_node)
                |                 - TomlKeyValuePrimitive (name=3)
                |                 - TomlTable ([a.b.c])
                |                     - TomlKeyValuePrimitive (name=4)
                |     - TomlTable ([[c]])
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlKeyValuePrimitive (name=5)
                |
        """.trimMargin(),
            parsedToml.prettyStr()
        )
    }

    @Test
    fun mixedTablesAndArrayOfTables2() {
        val string = """
            [a]
                name = 1
            
            [[a.b.c]]
                name = 2
            
            [[a.b.c]]
                name = 4
        """.trimIndent()


        val parsedToml = tomlParser.parseString(string)
        parsedToml.prettyPrint()
        assertEquals(
            """
                | - TomlFile (rootNode)
                |     - TomlTable ([a])
                |         - TomlKeyValuePrimitive (name=1)
                |         - TomlTable ([[a.b]])
                |             - TomlTable ([[a.b.c]])
                |                 - TomlArrayOfTablesElement (technical_node)
                |                     - TomlKeyValuePrimitive (name=2)
                |                 - TomlArrayOfTablesElement (technical_node)
                |                     - TomlKeyValuePrimitive (name=4)
                |
        """.trimMargin(),
            parsedToml.prettyStr()
        )
    }

    @Test
    fun mixedTablesAndArrayOfTables3() {
        val string = """
        [[fruit]]
        [fruit.physical]  
        color = "red"
        shape = "round"
        
        [[fruit]]
        [fruit.physical]  
        color = "red"
        shape = "round"
        """.trimIndent()

        val parsedToml = tomlParser.parseString(string)
        parsedToml.prettyPrint()

        assertEquals(
            """
                | - TomlFile (rootNode)
                |     - TomlTable ([[fruit]])
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlTable ([fruit.physical])
                |                 - TomlKeyValuePrimitive (color="red")
                |                 - TomlKeyValuePrimitive (shape="round")
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlTable ([fruit.physical])
                |                 - TomlKeyValuePrimitive (color="red")
                |                 - TomlKeyValuePrimitive (shape="round")
                |
               """.trimMargin(),
            parsedToml.prettyStr()
        )
    }

    @Test
    fun dottedKeysInsideTable() {
        val string = """
            [[table]]
            simple = 1
            item.simple = 2
            
            [[table]]
            item.complex = { a = 3, b = 4 }

            [[table]]
            simple = 5
            item.simple = 6
            item.complex = { a = 7, b = 8 }
        """.trimIndent()
        val parsedToml = tomlParser.parseString(string)

        assertEquals(
            """
                | - TomlFile (rootNode)
                |     - TomlTable ([[table]])
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlKeyValuePrimitive (simple=1)
                |             - TomlTable ([table.item])
                |                 - TomlKeyValuePrimitive (simple=2)
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlTable ([table.item])
                |                 - TomlTable ([table.item.complex])
                |                     - TomlKeyValuePrimitive (a=3)
                |                     - TomlKeyValuePrimitive (b=4)
                |         - TomlArrayOfTablesElement (technical_node)
                |             - TomlKeyValuePrimitive (simple=5)
                |             - TomlTable ([table.item])
                |                 - TomlKeyValuePrimitive (simple=6)
                |             - TomlTable ([table.item])
                |                 - TomlTable ([table.item.complex])
                |                     - TomlKeyValuePrimitive (a=7)
                |                     - TomlKeyValuePrimitive (b=8)
                |
               """.trimMargin(),
            parsedToml.prettyStr()
        )
    }
}
