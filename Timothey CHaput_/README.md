

# StringUtils

Les test généré pour les function `isBlank`, `IsEmpty` et `JoinWith`, test de manière adequate
les methode en question et detect tout les mutant relié a ces methode.

La class de test StringUtilsJoinWithTest fait cependant le choix particulier de faire un @BeforeEach qui
n'est utilisé que dans un des trois tests. C'est un manque de gout, mais ça ne fait pas que les tests ne sont
pas efficace.

Les teste générés pour LeftPad et Repeat sont à tout fin inutile. En fait la plupart dans eux échouait et
j'ai dû les corrigés manuellement. Au-delà de cela, il ne test pratiquement aucuns cas limit.

Il est aussi important de note que le code de StringUtils contient de nombreuse micro-optimization qui ont
eté crée à l'époque de java 1.4. Il n'est pas claire si ces optimisations sont encore util

Les tests générés pour StringUtils sont dans le sous-dossier `src/java/org/apache/tika/utils/generated/`
Les tests manuels pour StringUtils sont dans le fichier `src/java/org/apache/tika/utils/StringUtilsHumanTest.java`

## Test généré

### isBlank
Pour la fonction `isBlank`, chatUniTest a généré les tests suivent

```java
    @Test
    public void testIsBlank_NullString() {
        boolean result = StringUtils.isBlank(null);
        assertTrue(result);
    }

    @Test
    public void testIsBlank_EmptyString() {
        boolean result = StringUtils.isBlank("");
        assertTrue(result);
    }

    @Test
    public void testIsBlank_WhitespaceString() {
        boolean result = StringUtils.isBlank("   ");
        assertTrue(result);
    }

    @Test
    public void testIsBlank_NonBlankString() {
        boolean result = StringUtils.isBlank("Hello");
        assertFalse(result);
    }
```

Cela couvre les cas où

-  l'entrée est null.
-  l'entrée est un string vide.
-  l'entrée est un string composé de character qui sont "blank".
-  l'entrée est un string composé de character qui ne sont pas "blank".

Cela est une liste des bonnes catégories à tester. La seule chose à redire est que cela
n'explore pas la question de quel char sont considèrer comme "blank". Dans ce cas la définition
est basé sur la définition d'un char "blank" utilisé par la librairie std de java, c'est-à-dire une
liste bâtarde de character ASCII dont certain qui n'ont pas la propriété "White Space" selon unicode.


### IsEmpty
Pour la fonction isEmpty, chatUniTest à générer trois tests

```java
    @Test
    public void testIsEmpty_withNull() {
        // Arrange
        CharSequence cs = null;
        // Act
        boolean result = StringUtils.isEmpty(cs);
        // Assert
        assertTrue(result);
    }

    @Test
    public void testIsEmpty_withEmptyString() {
        // Arrange
        CharSequence cs = "";
        // Act
        boolean result = StringUtils.isEmpty(cs);
        // Assert
        assertTrue(result);
    }

    @Test
    public void testIsEmpty_withNonEmptyString() {
        // Arrange
        CharSequence cs = "Hello";
        // Act
        boolean result = StringUtils.isEmpty(cs);
        // Assert
        assertFalse(result);
    }
```


la function en question test si le string est null ou s'il est vide selon une fonction de la libraire std de
java.

Les tests générés sont acceptables pour cela. pour être honnête, je me questionne d'abord est avant tout
sur l'utilité d'une telle fonction.

Non mais sérieusement, si tu traites la valeur null comment équivalant au string vide, ça devrait être explicite
et la definition de la function n'est que 3 character de plus long que son invocation.

###  joinWith

Pour la fonction joinWith, chatUniTest à générer trois tests
```java
    private List<String> lines;

    @BeforeEach
    public void setUp() {
        lines = Arrays.asList("line1", "line2", "line3");
    }

    @Test
    public void testJoinWith_EmptyList() {
        Assertions.assertEquals("", StringUtils.joinWith(",", List.of()));
    }

    @Test
    public void testJoinWith_SingleElement() {
        assertEquals("line1", StringUtils.joinWith(",", List.of("line1")));
    }

    @Test
    public void testJoinWith_MultipleElements() {
        assertEquals("line1,line2,line3", StringUtils.joinWith(",", lines));
    }
```

L'utilisation d'un `@BeforeEach` qui n'est utilisé que pour un test est, à mon avis, de très mauvais gout.
Toutefois, les tests en question semblent quand même être suffisant.

### leftPad et repeat
Les tests génèrent pour leftPad et repeat sont terribles. La plupart ont dû être corrigé manuellement, car
il échoit. 

- Il y a de nombreux tests redondent. 
- Ils ne testent pas plains de cas limit.
- Pour plusieurs tests, pour savoir si le test est correct, il faut conter manuellement le nombre 
  de character d'un string qui en font plus que 20.

Il n'y a rien de bons à dire à propos de, ces tests. Je crois qu'une bonne partie
de la raison pour laquelle le llm n'était pas capable de générer de bons tests pour cette fonction vient du
fait qu'ils ne voient pas du text, mais plutôt une suite de token, ce qui fait qu'ils ont de la misère 
à remplir la tâche "produire un string de 20 fois le character 'x'" ou similaire.

Tous les mutant pour les quelle j'ai dû manuellement ajouter des tests provenait de ces deux methode.

## Test crée manuellement

`StringUtils.Repeat` contient une optimisation ou si l'entrée est un string vide,
le même string est retourné. J'ai donc écrit le test suivent pour ce cas. Le nombre de
repetition est 10, cela est simplement un entier strictement positif.

```java
    @Test
    public void testRepeat_emptyString() {
        // create a string that is not interned to test that
        // the function return the *same* string
        var input = new String("");
        var result =StringUtils.repeat(input, 10);
        assertSame(input, result);
    }
```

Un autre cas limit est quand on répète un string non vide 0 fois.
"aa" est simplement un string non vide.

```java
    @Test
    public void testRepeat_zeroRep() {
        var result =StringUtils.repeat("aa", 0);
        assertEquals("", result);
    }
```

Ce test permet de verifier l'exécution d'une optimisation ou si on demande de répéter un string
qui ne contain qu'un seul character et que le nombre de répetition demandé est inférieur à la limit
de paddage, la function qui ne répète qu'un character est utilisé à la place de la function qui répète
des strings.

Ce test ne test pas si l'optimisation a eu lieux, seulement que le résulta est correct dans les
cas ou cette optimisation devrait avoir lieux.

```java
    @Test
    public void testRepeat_singleCharString() {
        var result = StringUtils.repeat("a", 3);
        assertEquals(3, result.length());
        assertEquals("aaa", result);
    }
```

Le test suivent test le cas où l'optimisation précédant n'est pas éxecuté car le nombre
de répetition demandé au plus grand que la limit de paddage. Sachent que la limit de paddage est
très grand, j'utilise l'utilitaire de Regex de la librairie std de java peur testé le résulta

Ce test ne test pas si l'optimisation a eu lieux, seulement que le résulta est correct dans les
cas ou cette optimisation devrait avoir lieux.
```java
    @Test
    public  void testRepeat_singleCharStringOverPadLimit() {
        var result = StringUtils.repeat("a", StringUtils.PAD_LIMIT + 1);
        assertTrue(result.matches(String.format("a{%d}", StringUtils.PAD_LIMIT+1)));
    }
```

Il y a une optimisation de `repeat` specific au cas où l'entrée a deux character.

Ce test ne test pas si l'optimisation a eu lieux, seulement que le résulta est correct dans les
cas ou cette optimisation devrait avoir lieux.
```java
    @Test
    public  void testRepeat_twoChar() {
        var result = StringUtils.repeat("ab", 2);
        assertEquals("abab", result);
    }
```

la function `LeftPad` retourne null quand le string d'entré est null.
```java
    @Test
    public void testLeftPad_nullInput() {
        assertNull(StringUtils.leftPad(null, 0, ""));
    }
```

Quand le string de paddage a la valeur null, la function `LeftPad` traite cela comme si c'était un espace.
Les paramètres "a" et "2" ne sont que des placeholder.
```java
    @Test
    public void testLeftPad_nullPaddingStr() {
        var result =StringUtils.leftPad("a", 2, null);
        assertEquals(" a", result);
    }
```

La function `leftPad` pour string a une optimisation spécifique pour le cas ou il n'y a 
qu'un character de padding a ajouté.

Les paramètres "" et "b" ne sont que des placeholder.
Le paramère 1 s'assure qu'il y est exactement 1 character a ajouté.

Ce test ne test pas si l'optimisation a eu lieux, seulement que le résulta est correct dans les
cas où cette optimisation devrait avoir lieux.
```java
    @Test
    public void testLeftPad_onePadding() {
        var result =StringUtils.leftPad("", 1, "b");
        assertEquals("b", result);
    }
```

`leftPad` contient une optimisation ou si aucun padding est necessaire,
le même string est retourné. En utilisant un string non interné, on peut vérifier que
le même string est retourné.

Les paramètres "a" et 1 s'assure qu'aucune padding n'est necessaire
le paramètre "bb" est un placeholder.

Ce test vérifie la version au le padding est un string
```java
    @Test
    public void testLeftPad_noPadding() {
        // create a string that is not interned to test that
        // the function return the *same* string
        var input = new String("a");
        var result =StringUtils.leftPad(input, 1, "bb");
        assertSame(input, result);
    }
```

`leftPad` contient une optimisation ou si aucun padding est necessaire,
le même string est retourné. En utilisant un string non interné, on peut vérifier que
le même string est retourné.

Les paramètres "a" et 1 s'assure qu'aucune padding n'est necessaire
le paramètre "bb" est un placeholder.

Ce test vérifie la version au le padding est un char
```java
    @Test
    public void testLeftPad_noCharPadding() {
        // create a string that is not interned to test that
        // the function return the *same* string
        var input = new String("a");
        var result =StringUtils.leftPad(input, 1, 'b');
        assertSame(input, result);
    }
```

Le cas ou le string de padding est plus long que la quantité de padding necessaire est un cas limit qu'il faut tester.
Les paramètres "a", 3 et "12345" s'assure que la longueur du string de padding (6) est plus long que la quantité de
padding necessaire (2).
```java
    @Test
    public void testLeftPad_partialPadding() {
        var result =StringUtils.leftPad("a", 3, "123456");
        assertEquals("12a", result);
    }
```

`leftPad` contient une optimisation spécifique au cas où la quantité de padding necessaire est égal à la longueur du
string de padding

Les paramètres "a", 3 et "12" s'assure que la longueur du string de padding (2) est plus long que la quantité de
padding necessaire (2).

Ce test ne test pas si l'optimisation a eu lieux, seulement que le résulta est correct dans les
cas où cette optimisation devrait avoir lieux.
```java 
    @Test
    public void testLeftPad_fullPadding() {
        var result =StringUtils.leftPad("a", 3, "12");
        assertEquals("12a", result);
    }
```

Dans les cas où le padding est constitué d'un seul character répété et que la quantité de padding necessaire est plus
petite que la limit de padding, utilisé la version de `leftPad` avec le charter de padding est considéré plus efficace
que la version avec le string de padding.

De ce fait si une de version de `leftPad` determine que l'autre serait plus efficace, il rend vois vers celle-ci.
Si la condition sur la longueur de padding necessaire est inversé, cela peut créer une boucle infinie ce qui est un comportement
observable.

De ce fait, les deux tests suivent qui envoi des exemples aux deux versions de `leftPad`
qui sont sur la limit de cette condition.

Dans ce test l'oracle est basé sur les utilitaires de Regex de la librairie std de java. Cependant,
dans ce cas si ce n'est pas cet oracle qui réussi à montrer la défaillance, mais plutôt l'oracle général de est-ce que
le test génère une boucle infinie. Cela peut être détecté via un stackOverFlow ou via un timeout sur le test. 

```java
    @Test
    public void testLeftPad_padCharOverPaddingLimit() {
        var result =StringUtils.leftPad("b", StringUtils.PAD_LIMIT + 2, 'a');
        assertTrue(result.matches(String.format("a{%d}b", StringUtils.PAD_LIMIT + 1)));
    }
```

## Mutant restant

Il reste six mutants qui ne peuvent pas être attrapé par des tests fonctionnels.

Dans 2 cas, il s'agit de mutant qui produise des programs strictement équivalant

Dans 4 cas, il s'agit de mutant qui font en sorte qu'une optimisation prévue n'est pas suivie,
mais qui fournissent le même résulta.

