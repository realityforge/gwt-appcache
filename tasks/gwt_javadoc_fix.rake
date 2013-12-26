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
