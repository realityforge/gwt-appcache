require 'buildr/single_intermediate_layout'
require 'buildr/git_auto_version'
require 'buildr/top_level_generate_dir'

# Ugly hack required as the gwt jars cause the javadoc tool heart ache
module Buildr
  module DocFix #:nodoc:
    include Extension
    after_define(:doc) do |project|
      project.doc.classpath.clear
    end
  end
  class Project #:nodoc:
    include DocFix
  end
end

desc 'GWT Appcache Linker and server support'
define 'gwt-appcache' do
  project.group = 'org.realityforge.gwt.appcache'
  compile.options.source = '1.6'
  compile.options.target = '1.6'
  compile.options.lint = 'all'

  project.version = ENV['PRODUCT_VERSION'] if ENV['PRODUCT_VERSION']

  define 'client' do
    compile.with :gwt_user, :gwt_dev

    test.using :testng
    test.with :mockito

    package(:jar).include("#{_(:source, :main, :java)}/*")
    package(:sources)
    package(:javadoc)
  end

  define 'linker' do
    compile.with :gwt_user, :gwt_dev, project('server')

    test.using :testng
    test.with :mockito

    package(:jar)
    package(:sources)
    package(:javadoc)
  end

  define 'server' do
    compile.with :javax_servlet, :gwt_user, :gwt_dev

    test.using :testng
    test.with :mockito

    package(:jar)
    package(:sources)
    package(:javadoc)
  end
end
