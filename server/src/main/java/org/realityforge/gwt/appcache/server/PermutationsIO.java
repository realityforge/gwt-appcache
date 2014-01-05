package org.realityforge.gwt.appcache.server;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class PermutationsIO
{
  public static final String PERMUTATIONS_DESCRIPTOR_FILE_NAME = "permutations.xml";
  private static final String PERMUTATION_NODE = "permutation";
  private static final String PERMUTATION_NAME = "name";
  private static final String PERMUTATIONS = "permutations";

  private PermutationsIO()
  {
  }

  public static List<SelectionDescriptor> deserialize( final InputStream stream )
    throws Exception
  {
    final List<SelectionDescriptor> descriptors = new ArrayList<SelectionDescriptor>();

    final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( stream );
    final Element permutationsNode = document.getDocumentElement();
    final String tagName = permutationsNode.getTagName();
    if ( !PERMUTATIONS.equals( tagName ) )
    {
      throw new Exception( "unexpected xml structure: Expected node : '" + PERMUTATIONS + "' got: '" + tagName + "'" );
    }

    final NodeList permutationsChildren = permutationsNode.getChildNodes();

    final int length = permutationsChildren.getLength();
    for ( int i = 0; i < length; i++ )
    {
      final Node node = permutationsChildren.item( i );
      if ( node.getNodeType() != Node.ELEMENT_NODE )
      {
        continue;
      }
      final Element permutationNode = (Element) node;
      handlePermutation( descriptors, permutationNode );
    }
    return Collections.unmodifiableList( descriptors );
  }

  private static void handlePermutation( final List<SelectionDescriptor> descriptors, final Element permutationNode )
    throws Exception
  {
    final String strongName = permutationNode.getAttribute( PERMUTATION_NAME );

    final ArrayList<BindingProperty> list = new ArrayList<BindingProperty>();

    final NodeList variableNodes = permutationNode.getChildNodes();
    for ( int i = 0; i < variableNodes.getLength(); i++ )
    {
      final Node item = variableNodes.item( i );
      if ( Node.ELEMENT_NODE != item.getNodeType() )
      {
        continue;
      }
      final Element variables = (Element) item;
      final String varKey = variables.getTagName();
      final NodeList childNodes = variables.getChildNodes();
      if ( childNodes.getLength() != 1 )
      {
        throw new Exception( "Unexpected XML Structure: Expected property value" );
      }

      final String varValue = childNodes.item( 0 ).getNodeValue();
      list.add( new BindingProperty( varKey, varValue ) );
    }
    descriptors.add( new SelectionDescriptor( strongName, list ) );
  }

  public static String serialize( final List<SelectionDescriptor> descriptors )
    throws Exception
  {
    final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    final Element permutationsNode = document.createElement( PERMUTATIONS );
    document.appendChild( permutationsNode );

    for ( final SelectionDescriptor descriptor : descriptors )
    {
      final Element node = document.createElement( PERMUTATION_NODE );
      node.setAttribute( PERMUTATION_NAME, descriptor.getPermutationName() );
      permutationsNode.appendChild( node );
      for ( final BindingProperty b : descriptor.getBindingProperties() )
      {
        final Element variable = document.createElement( b.getName() );
        variable.appendChild( document.createTextNode( b.getValue() ) );
        node.appendChild( variable );
      }
    }
    return transformDocumentToString( document );
  }

  private static String transformDocumentToString( final Document document )
    throws Exception
  {
    final StringWriter xml = new StringWriter();
    final Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
    transformer.transform( new DOMSource( document ), new StreamResult( xml ) );
    return xml.toString();
  }
}
