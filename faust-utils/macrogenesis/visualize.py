"""
Transform a networkx graph generated by the ``macrogenesis`` module into a layouted and styled output via
 graphviz.
"""
import base64
import logging
import math
import os.path
import shutil
import textwrap

import networkx

import faust
import graph

# styles and defaults
KEY_HIGHLIGHT = 'highlight'
VALUE_TRUE = 'true'

DEFAULT_EDGE_WEIGHT = '1'
STYLE_ABSOLUTE_DATING_CLUSTER_COLOR = 'grey'
STYLE_ABSOLUTE_DATING_COLOR = '#ffffff00'

# this causes errors in graphviz in big graphs
# DEFAULT_EDGE_PENWIDTH = '1'
EDGE_STYLES = [(graph.KEY_RELATION_NAME, 'temp-pre', 'weight', '1'),
               (graph.KEY_RELATION_NAME, 'temp-pre', 'style', 'solid'),
               (graph.KEY_RELATION_NAME, 'temp-syn', 'style', 'dashed'),
               (graph.KEY_RELATION_NAME, 'temp-syn', 'weight', '0.5'),
               (graph.KEY_RELATION_NAME, 'temp-syn', 'arrowhead', 'none'),

               (graph.KEY_RELATION_NAME, graph.VALUE_IMPLICIT_FROM_ABSOLUTE, 'weight', '10'),
               (graph.KEY_RELATION_NAME, graph.VALUE_IMPLICIT_FROM_ABSOLUTE, 'color', 'grey')]

NODE_STYLES = [(KEY_HIGHLIGHT, VALUE_TRUE, 'fillcolor', 'black'),
               (KEY_HIGHLIGHT, VALUE_TRUE, 'fontcolor', 'white'),
               (KEY_HIGHLIGHT, VALUE_TRUE, 'style', 'filled')]



def _label_from_uri(uri):
    """Returns a readable label from a uri of a document, inscription, bibliographical source, etc."""
    # label = uri[len('faust://'):]

    if (uri.startswith('faust://document/wa/')):
        return uri[len('faust://document/wa/'):]
    if (uri.startswith('faust://inscription/wa')):
        return uri[len('faust://inscription/wa/'):]
    if (uri.startswith('faust://document/')):
        return uri[len('faust://document/'):]
    if (uri.startswith('faust://inscription/')):
        return uri[len('faust://inscription/'):]
    if (uri.startswith('faust://bibliography/')):
        return uri[len('faust://bibliography/'):]
    if (uri.startswith('faust://')):
        return uri[len('faust://'):]

    #if uri.rindex('/') >= 0:
    #   return uri[uri.rindex('/') + 1:]

    return uri

def _set_node_url(attr, url):
    """Set the URL for a hyperlink for a node"""
    link_suffix = 'html'
    attr['URL'] = ('%s.%s' % (url, link_suffix))
    attr['target'] = "_top"

def _apply_agraph_styles(agraph, edge_labels=False):
    """Set style properties of a dot graph for rendering"""
    for edge in agraph.edges():
        edge.attr['weight'] = DEFAULT_EDGE_WEIGHT
        #edge.attr['penwidth'] = DEFAULT_EDGE_PENWIDTH



        if graph.KEY_BIBLIOGRAPHIC_SOURCE in edge.attr.keys():
            edge_tooltip = 's. %s &#013;&#013;%s    ->    %s&#013;&#013;Source file: %s' % (
                _label_from_uri(edge.attr[graph.KEY_BIBLIOGRAPHIC_SOURCE]),
                _label_from_uri(edge[0]), _label_from_uri(edge[1]),
                edge.attr[graph.KEY_SOURCE_FILE])
            edge.attr['tooltip'] = edge_tooltip
            edge.attr['labeltooltip'] = edge_tooltip

            if edge_labels:
                edge.attr['label'] = _label_from_uri(edge.attr[graph.KEY_BIBLIOGRAPHIC_SOURCE])

        for (genetic_key, genetic_value, style_key, style_value) in EDGE_STYLES:
            if genetic_key in edge.attr.keys() and edge.attr[genetic_key] == genetic_value:
                edge.attr[style_key] = style_value

    for node in agraph.nodes():

        if graph.KEY_NODE_TYPE in node.attr.keys() and node.attr[graph.KEY_NODE_TYPE] == graph.VALUE_ITEM_NODE:
            # link to subgraph for single node neighborhood
            _set_node_url(node.attr, _highlighted_base_filename(node))
            node.attr['label'] = _label_from_uri(node)
            node.attr['tooltip'] = '%s &#013;&#013; %s ' \
                                   % (_label_from_uri(node), node)

        for (genetic_key, genetic_value, style_key, style_value) in NODE_STYLES:
            if genetic_key in node.attr.keys() and node.attr[genetic_key] == genetic_value:
                node.attr[style_key] = style_value



def _agraph_from(graph, edge_labels=False):
    """Returns a graphviz graph from a networkx graph"""
    # append_absolute_date_nodes(graph)

    logging.info(" Generating agraph.")

    agraph = networkx.nx_agraph.to_agraph(graph)

    # agraph.graph_attr['overlap'] = "1"
    # agraph.graph_attr['concentrate'] = "true"

    _visualize_absolute_datings(agraph)

    _apply_agraph_styles(agraph, edge_labels)
    agraph.graph_attr['tooltip'] = ' '

    return agraph


def _visualize_absolute_datings(agraph):
    """
    Add visualisations for absolute datings to a graphviz graph.
    It is (ab)using the subgraph feature, so that a subgraph contains the inscription node and a node for all absolute
    dating labels.
    """
    cluster_index = 0
    absolute_dating_index = 0
    dated_nodes = []

    for node in agraph.nodes():
        if graph.KEY_ABSOLUTE_DATINGS_PICKLED in node.attr.keys():
            logging.debug("Adding cluster for absolute datings of node {0}".format(node))
            absolute_datings = graph.deserialize_from_graphviz(node.attr[graph.KEY_ABSOLUTE_DATINGS_PICKLED])
            absolute_dating_nodes = []
            for absolute_dating in absolute_datings:
                date_id = 'date_{0}'.format(absolute_dating_index)
                date_label = str(absolute_dating)
                agraph.add_node(date_id, label=date_label, date_average=absolute_dating.average, shape='box',
                                color=STYLE_ABSOLUTE_DATING_COLOR, tooltip='s. %s &#013;&#013;Source file: %s'
                                % (_label_from_uri(absolute_dating.bibliographic_source), absolute_dating.source_file))
                absolute_dating_nodes.append(date_id)
                absolute_dating_index += 1
            cluster_id = 'cluster{0}'.format(cluster_index)
            agraph.add_subgraph(nbunch=absolute_dating_nodes + [node], name=cluster_id,
                                color=STYLE_ABSOLUTE_DATING_CLUSTER_COLOR)
            cluster_index += 1

def html_template(content):
    return '<html><head><script src="js/svg-pan-zoom.min.js" type="text/javascript"></script></head>'\
    '<body>%s</body>'\
    '</html>' % content


def _write_agraph_layout (agraph, dir, basename):
    """Layout a graphviz graph and write it to disk as svg with an html wrapper."""
    agraph.write(os.path.join(dir, '%s.%s' % (basename, 'dot')))
    agraph.layout(prog='dot')
    svg_filename = '%s.%s' % (basename, 'svg')
    agraph.draw(os.path.join(dir, svg_filename))
    html_filename = os.path.join(dir, '%s.%s' % (basename, 'html'))
    with open(html_filename, mode='w') as html_file:
        html_file.write(html_template('<object id="genesis_graph" data="%s" type="image/svg+xml" style="width: 100%%;\
        height: 100%%; border:1px solid black; "></object> <script type="text/javascript">window.onload = function()\
         {svgPanZoom("#genesis_graph", {zoomEnabled: true, controlIconsEnabled: true});};</script>' % svg_filename))


def _highlighted_base_filename (highlighted_node_url):
    """Calculate and return a unique filename for a subgraph of the neighborhood of a certain ("highlighted") node"""
    return '20_highlighted_%s' % base64.urlsafe_b64encode(highlighted_node_url)

def visualize():
    """
    Lay out, style and write to disk the macrogenesis graphs.
    :return: list of pairs ``(link_text, relative_path)`` to the generated html pages
    """
    output_dir = faust.config.get("macrogenesis", "output-dir")
    # copy resources
    try:
        shutil.copytree('macrogenesis/resources/js', os.path.join(output_dir, 'js'))
    except OSError as e:
        logging.warn(e)


    #collect hyperlinks to selected graphs for the TOC as [(link_text_1, relative_link_to_file_1), ...]
    links = []

    # draw raw input data
    graph_imported = graph.import_graph()
    logging.info("Generating raw data graph.")
    #UUU write_agraph_layout(agraph_from(graph_imported), output_dir, '00_raw_data')

    # highlight a single node and its neighbors
    # highlighted_node = 'faust://document/wa/2_I_H.17'
    for highlighted_node in graph_imported:
        highlighted_bunch = list(networkx.all_neighbors(graph_imported, highlighted_node))
        highlighted_bunch.append(highlighted_node)
        graph_highlighted_subgraph = graph_imported.subgraph(nbunch=highlighted_bunch).copy()
        graph_highlighted_subgraph.node[highlighted_node][KEY_HIGHLIGHT]= VALUE_TRUE
        graph.insert_minimal_edges_from_absolute_datings(graph_highlighted_subgraph)

        #agraph_highlighted_subgraph.node_attr[highlighted_node]['color'] = 'red'
        _write_agraph_layout(_agraph_from(graph_highlighted_subgraph), output_dir,
                             _highlighted_base_filename(highlighted_node))
        # try again with edge labels, sometimes this genereats segfaults in dot, then there is the already
        # generated graph without edge labels already there
        try:
            _write_agraph_layout(_agraph_from(graph_highlighted_subgraph, edge_labels=True), output_dir,
                                 _highlighted_base_filename(highlighted_node))
        except Exception:
            logging.error("Dot failed generating a graph")


    # add relationships implicit in absolute datings
    logging.info("Generating graph with implicit absolute date relationships.")
    graph_absolute_edges = graph_imported
    graph.insert_minimal_edges_from_absolute_datings(graph_absolute_edges)
    del graph_imported
    base_filename_absolute_edges = '10_absolute_edges'
    _write_agraph_layout(_agraph_from(graph_absolute_edges), output_dir, base_filename_absolute_edges)
    links.append(('Raw datings (relative and absolute)', '%s.html' % base_filename_absolute_edges))
    # again with edge labels
    # TODO this breaks graphviz
    # agraph_absolute_edges_edge_labels = agraph_from(graph_absolute_edges, edge_labels=True)
    # write_agraph_layout(agraph_absolute_edges_edge_labels, output_dir, '15_absolute_edges_edge_labels')

    logging.info("Generating condensation.")
    strongly_connected_components = list(networkx.strongly_connected_components(graph_absolute_edges))

    #condensation
    graph_condensation = networkx.condensation(graph_absolute_edges, scc=strongly_connected_components)
    for node in graph_condensation:
        label = ', '.join([_label_from_uri(uri) for uri in graph_condensation.node[node]['members']])
        label_width = int(2 * math.sqrt(len(label)))
        graph_condensation.node[node]['label'] = textwrap.fill(label, label_width, break_long_words=False).replace('\n','\\n')
        component_filename_pattern = '16_strongly_connected_component_%i'
        # make a hyperlink to subgraph of the component
        if len(graph_condensation.node[node]['members']) > 1:
            _set_node_url(graph_condensation.node[node], component_filename_pattern % node)
        else:
            # TODO just link to normal neighborhood subgraph for single nodes
            pass

    base_filename_condensation = '15_condensation'
    _write_agraph_layout(_agraph_from(graph_condensation), output_dir, base_filename_condensation)
    links.append(('Condensation', '%s.html' % base_filename_condensation))

    for (component_index, component) in enumerate(strongly_connected_components):
        # don't generate subgraphs consisting of a single node
        if len(component) > 1:
            graph_component = graph_absolute_edges.subgraph(nbunch=component).copy()
            #macrogenesis.insert_minimal_edges_from_absolute_datings(graph_component)
            # , edge_labels=True)
            _write_agraph_layout(_agraph_from(graph_component), output_dir,
                                 component_filename_pattern % (component_index))

    # transitive closure, don't draw
    logging.info("Generating transitive closure graph.")
    transitive_closure = networkx.transitive_closure(graph_absolute_edges)
    logging.info("{0} nodes, {1} edges in transtive closure.".format(transitive_closure.number_of_nodes(),
                                                                     transitive_closure.number_of_edges()))
    agraph_transitive_closure = _agraph_from(transitive_closure)

    # draw transitive reduction
    logging.info("Generating transitive reduction graph.")
    agraph_transitive_reduction = agraph_transitive_closure.tred(copy=True)
    logging.info("{0} nodes, {1} edges in transtive reduction.".format(agraph_transitive_reduction.number_of_nodes(),
                                                                      agraph_transitive_reduction.number_of_edges()))
    base_filename_transitive_reduction = '30_transitive_reduction'
    _write_agraph_layout(agraph_transitive_reduction, output_dir, base_filename_transitive_reduction)
    links.append(('Transitive reduction', '%s.html' % base_filename_transitive_reduction))
    return links


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    visualize()
