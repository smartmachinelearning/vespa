// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#include "featureexecutor.h"
#include <vespa/vespalib/util/classname.h>

namespace search::fef {

FeatureExecutor::FeatureExecutor() = default;


vespalib::string
FeatureExecutor::getClassName() const
{
    return vespalib::getClassName(*this);
}

bool
FeatureExecutor::isPure()
{
    return false;
}

void
FeatureExecutor::handle_bind_inputs()
{
}

void
FeatureExecutor::handle_bind_outputs()
{
}

void
FeatureExecutor::handle_bind_match_data(const MatchData &)
{
}

void
FeatureExecutor::bind_inputs(vespalib::ConstArrayRef<LazyValue> inputs)
{
    _inputs.bind(inputs);
    handle_bind_inputs();
}

void
FeatureExecutor::bind_outputs(vespalib::ArrayRef<NumberOrObject> outputs)
{
    _outputs.bind(outputs);
    _outputs.set_docid(-1);
    handle_bind_outputs();
}

void
FeatureExecutor::copy_inputs(const FeatureExecutor::Inputs &inputs)
{
    _inputs = inputs;
    handle_bind_inputs();
}

void
FeatureExecutor::copy_outputs(const FeatureExecutor::Outputs &outputs)
{
    _outputs = outputs;
    handle_bind_outputs();
}

void
FeatureExecutor::bind_match_data(const MatchData &md)
{
    handle_bind_match_data(md);
}

}
