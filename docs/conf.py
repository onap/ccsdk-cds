from docs_conf.conf import *

branch = 'latest'
doc_url = 'https://docs.onap.org/projects'
master_doc = 'index'

linkcheck_ignore = [
    'http://localhost',
]

extensions = ['sphinx_tabs.tabs', 'sphinxcontrib.swaggerdoc', 'sphinx.ext.intersphinx']

intersphinx_mapping = {}

intersphinx_mapping['onap-integration'] = ('{}/onap-integration/en/%s'.format(doc_url) % branch, None)

html_last_updated_fmt = '%d-%b-%y %H:%M'

def setup(app):
    app.add_css_file("css/ribbon.css")
