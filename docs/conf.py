from docs_conf.conf import *

branch = 'latest'
doc_url = 'https://docs.onap.org/projects'
master_doc = 'index'

linkcheck_ignore = [
    'http://localhost',
]

<<<<<<< HEAD   (38bf68 add toggle variable ansible fire failure to awx function)
extensions = ['sphinx_tabs.tabs']
=======
extensions = ['sphinx_tabs.tabs', 'sphinxcontrib.swaggerdoc', 'sphinx.ext.intersphinx']
>>>>>>> CHANGE (ca623d CDS add vFW Use Case from Integration project)

intersphinx_mapping = {}

intersphinx_mapping['onap-integration'] = ('{}/onap-integration/en/%s'.format(doc_url) % branch, None)

html_last_updated_fmt = '%d-%b-%y %H:%M'

def setup(app):
    app.add_css_file("css/ribbon.css")
