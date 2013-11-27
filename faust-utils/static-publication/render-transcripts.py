import requests, urllib, os, os.path, sys, tempfile, shutil
from subprocess import call, check_call

context_path = 'http://localhost:8080/faustedition/'

manuscript_urls = [
    # "artige Handschriften"

    'http://localhost:8080/faustedition/document/gedichte/gsa_390162.xml',
    #'http://localhost:8080/faustedition/document/gedichte/gsa_390603.xml',
    #'http://localhost:8080/faustedition/document/verschiedenes/gsa_390376.xml',
    'http://localhost:8080/faustedition/document/verschiedenes/gsa_390200.xml',
    #'http://localhost:8080/faustedition/document/archival/gsa/GSA_35-N_44.xml',
    'http://localhost:8080/faustedition/document/archival/gsa/GSA_68-878_Mit_drey_Blumen_Straeussen.xml',
    'http://localhost:8080/faustedition/document/archival/gsa/GSA_25-W_1419a.xml',
    #'http://localhost:8080/faustedition/document/archival/gsa/GSA_25-XIX-3_Filmnr_1427_360.xml',
    'http://localhost:8080/faustedition/document/faust/1/gsa_390789.xml',
    'http://localhost:8080/faustedition/document/faust/1/gsa_390753.xml',
    'http://localhost:8080/faustedition/document/faust/1/gsa_390263.xml',
    'http://localhost:8080/faustedition/document/faust/2.3/gsa_390838.xml',
    # 'http://localhost:8080/faustedition/document/faust/2.3/gsa_391484.xml',
    'http://localhost:8080/faustedition/document/faust/2.5/gsa_391525.xml',
    'http://localhost:8080/faustedition/document/faust/2.5/gsa_391507.xml',
    # 'http://localhost:8080/faustedition/document/faust/2.5/gsa_391508.xml',
    'http://localhost:8080/faustedition/document/faust/2.5/gsa_389771.xml',
    'http://localhost:8080/faustedition/document/faust/2.4/gsa_390704.xml',
    # 'http://localhost:8080/faustedition/document/faust/2.4/gsa_390157.xml',
    'http://localhost:8080/faustedition/document/faust/2/gsa_390777.xml',
    'http://localhost:8080/faustedition/document/faust/2.2/gsa_391436.xml',
    'http://localhost:8080/faustedition/document/faust/2.2/gsa_391492.xml',
    'http://localhost:8080/faustedition/document/faust/2.2/gsa_390656.xml',
    'http://localhost:8080/faustedition/document/faust/2.1/gsa_390871.xml',
    'http://localhost:8080/faustedition/document/faust/2.1/gsa_390434.xml',
    # 'http://localhost:8080/faustedition/document/faust/2.1/gsa_389892.xml',
    'http://localhost:8080/faustedition/document/faust/2.1/gsa_390893.xml',
    'http://localhost:8080/faustedition/document/faust/2.1/gsa_390188.xml',
    'http://localhost:8080/faustedition/document/paralipomena/gsa_390887.xml',
    'http://localhost:8080/faustedition/document/paralipomena/gsa_390778.xml',
    'http://localhost:8080/faustedition/document/paralipomena/gsa_390259.xml',
    'http://localhost:8080/faustedition/document/paralipomena/gsa_391360.xml',
    'http://localhost:8080/faustedition/document/paralipomena/gsa_389847.xml',
    'http://localhost:8080/faustedition/document/paralipomena/gsa_390093.xml',
    'http://localhost:8080/faustedition/document/paralipomena/gsa_389879.xml',
    'http://localhost:8080/faustedition/document/paralipomena/gsa_390405.xml',
    # 'http://localhost:8080/faustedition/document/paralipomena/gsa_391368.xml',
    'http://localhost:8080/faustedition/document/paralipomena/gsa_390387.xml'
    ]

latex_header = """\\documentclass[11pt,oneside]{book} 
\\usepackage{makeidx}
\\usepackage{graphicx}
\\usepackage[german]{babel} 
\\usepackage[utf8]{inputenc}
\usepackage{hyperref}
\hypersetup{
    colorlinks,
    citecolor=black,
    filecolor=black,
    linkcolor=black,
    urlcolor=black
}
\DeclareUnicodeCharacter{00D7}{x}
\\begin{document} 
\\author{Johann Wolfgang Goethe} 
\\title{Faust. Historisch-kritische Ausgabe} 
\\date{\\today} 
\\maketitle
\\frontmatter 
\\setcounter{secnumdepth}{0}
\\setcounter{tocdepth}{1}
\\tableofcontents 
\\mainmatter 
\\chapter{Handschriften}
"""

latex_footer = """
\\backmatter 
\\printindex
\\end{document}
"""


def extract_pages(mu):
    result = [];
    if mu['type'] == 'page':
        # print "   seite: " + str(mu['transcript'] if 'transcript' in mu else '--')
        result.append(mu)
    for child in mu['contents']:
        result.extend(extract_pages(child))
    return result

def get_pageurls(url):
    answer = requests.get(url).json()
    answer_pages = extract_pages(answer)
    return [a_page['transcript']['source'] for a_page in answer_pages if 'transcript' in a_page]

def get_doc_src(doc_data):
    doc_src = doc_data['document-source']
    return doc_src if doc_src else "Keine URI"

def quote_filename(filename):
    return urllib.quote_plus(filename.encode('utf-8').replace('.', '_') + u'.png').replace('%', '_')

def generate_out_filepath(page_url, tmp_dir):
        out_filename = quote_filename(page_url)
        return os.path.join(tmp_dir, 'graphics',  out_filename)

def render_document(url, tmp_dir):    
    print "document ", url
    for (i, page_url) in enumerate(get_pageurls(url)):
        #pagenumbers starting from 1
        pagenum = i + 1 
        out_filepath = generate_out_filepath(page_url, tmp_dir)
        print " rendering page ", pagenum, ": ", page_url
        if not os.path.exists(out_filepath):
            print "   (rendering to      " + out_filepath  + ")"
            check_call(['phantomjs', 'render-transcript.js', url + '?view=transcript-bare#' + str(i+1), out_filepath]) 
        else:
            print "   (already exists at " + out_filepath + ")"

def metadata_if_exists(value):
    return u'\\noindent{}' + value + u'\n\n' if value and value != "none" else ""
        
def generate_document_overview(url, doc_data):
    result = u''
    doc_src = get_doc_src(doc_data)
    result = result +  u'\clearpage\n'
    result = result + u'\\vfill\n{}'
    result = result + u'\section{' + doc_data['name'] + u'}\n\n\n'
    result = result + metadata_if_exists(doc_data['callnumber.wa-faust'])
    result = result + metadata_if_exists(doc_data['callnumber.gsa-1'])
    result = result + metadata_if_exists(doc_data['callnumber.gsa-2'])
    result = result + u'\\begin{verbatim}\n' + doc_src + u'\n\\end{verbatim}\n\n'
    num_pages = len(get_pageurls(url))
    result = result + str(num_pages) + u' Seiten\n\n'
    #result = result + u'\\begin{verbatim}\n'
    #if doc_data['note']: result = result + doc_data['note'] + u'\n'
    #result = result + u'\\end{verbatim}\n'

    result = result + u'\\vfill\n{}'
    return result

def generate_latex(manuscript_urls, tmp_dir):
    result = ''
    for url in manuscript_urls:
        # try:
            doc_data = requests.get(url).json()
            result = result + generate_document_overview(url, doc_data)
            for (i, page_url) in enumerate(get_pageurls(url)):
                pagenum = i + 1
                #if pagenum != 1:
                result = result + u'\clearpage\n'
                result = result + u'\subsection{Seite ' + str(pagenum) + "}\n"
                result = result + u'\\vfill{}\n'
                # TODO hack
                if "self/none"  in page_url:
                    result = result + u"[Leere Seite]"
                else: 
                    result = result + u'\includegraphics[width=\\linewidth,height=0.9\\textheight,keepaspectratio]{' + generate_out_filepath(page_url, tmp_dir)  + u'}\n'
        # except Exception as e:
        #     result = result + 'Fehler beim Einlesen der Handschriftenbeschreibung \n\n'
        #     print "Error: ", e
    return result

def main():

    if len(sys.argv) < 2 or len(sys.argv) > 3:
        print 'usage: render-transcripts.py pdf_result [tmp_dir]'
        print '   tmp_dir caches rendered graphics to be reused'
        exit(-1)

    pdf_result = os.path.abspath(sys.argv[1])
    tmp_dir = os.path.abspath(sys.argv[2]) if len(sys.argv) > 2 else tempfile.mkdtemp()

    if not os.path.isdir(tmp_dir):
        os.mkdir(tmp_dir)

    for url in manuscript_urls:
        try:
            render_document(url, tmp_dir)
        except Exception as e:
            print "Error rendering document: ", e

    latex_tmp_dir = tempfile.mkdtemp()
    latex_filename = os.path.join(latex_tmp_dir, 'faust.tex')
    latex_out = open(latex_filename, 'w')

    print "writing latex to " + latex_filename
    latex_out.write(latex_header)
    latex_out.write(generate_latex(manuscript_urls, tmp_dir).encode('utf-8'))
    latex_out.write(latex_footer)
    latex_out.close()

    os.chdir(latex_tmp_dir)
    # twice for toc indexing
    check_call(['pdflatex', '-output-directory ' + latex_tmp_dir, latex_filename])
    check_call(['pdflatex', '-output-directory ' + latex_tmp_dir, latex_filename])
    shutil.copyfile(os.path.join(latex_tmp_dir, "faust.pdf"), pdf_result)

if __name__ == '__main__':
    main()

