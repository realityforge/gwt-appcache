require 'buildr/git_auto_version'
require 'buildr/top_level_generate_dir'

desc 'GWT Appcache Linker and server support'
define 'gwt-appcache' do
  project.group = 'org.realityforge.gwt.appcache'
  compile.options.source = '1.6'
  compile.options.target = '1.6'
  compile.options.lint = 'all'

  project.version = ENV['PRODUCT_VERSION'] if ENV['PRODUCT_VERSION']

  define 'server' do
    compile.with :javax_servlet, :gwt_user, :gwt_dev

    package(:jar)
  end
end
